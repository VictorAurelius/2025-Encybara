package utc.englishlearning.Encybara.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.response.chatgpt.ChatGPTEvaluateResponse;
import utc.englishlearning.Encybara.exception.ChatGPTException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.HashMap;

@Service
@Slf4j
public class ChatGPTService {
    private static final String CHAT_URL = "https://chat.openai.com/";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @Value("${chatgpt.session-token:#{null}}")
    private String sessionToken;

    @Value("${chatgpt.clearance-token:#{null}}")
    private String clearanceToken;

    @Value("${chatgpt.callback-url:#{null}}")
    private String callbackUrl;

    @Value("${chatgpt.user-agent}")
    private String userAgent;

    @Value("${playwright.browser.headless:true}")
    private boolean headless;

    @Value("${playwright.browser.slowmo:50}")
    private int slowMo;

    @Value("${playwright.timeout:45000}")
    private int timeout;

    @PostConstruct
    private void initialize() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setSlowMo(slowMo));
            setupBrowserContext();
            log.info("Browser initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize browser: ", e);
            throw new ChatGPTException("Failed to initialize browser", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void setupBrowserContext() {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept-Language", "en-US,en;q=0.9");
            headers.put("Sec-Fetch-Dest", "document");
            headers.put("Sec-Fetch-Mode", "navigate");
            headers.put("Sec-Fetch-Site", "none");
            headers.put("Sec-Fetch-User", "?1");
            headers.put("Upgrade-Insecure-Requests", "1");

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setUserAgent(userAgent)
                    .setViewportSize(1920, 1080)
                    .setLocale("en-US")
                    .setTimezoneId("Asia/Bangkok")
                    .setExtraHTTPHeaders(headers);

            context = browser.newContext(contextOptions);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                List<Cookie> cookies = List.of(
                        new Cookie("__Secure-next-auth.session-token", sessionToken)
                                .setDomain("chat.openai.com")
                                .setPath("/")
                                .setSecure(true)
                                .setHttpOnly(true),
                        new Cookie("cf_clearance", clearanceToken)
                                .setDomain(".chat.openai.com")
                                .setPath("/")
                                .setSecure(true)
                                .setHttpOnly(true),
                        new Cookie("__Secure-next-auth.callback-url", CHAT_URL)
                                .setDomain("chat.openai.com")
                                .setPath("/")
                                .setSecure(true)
                                .setHttpOnly(true),
                        new Cookie("_puid", "user-" + System.currentTimeMillis())
                                .setDomain(".chat.openai.com")
                                .setPath("/")
                                .setSecure(true)
                                .setHttpOnly(true));
                context.addCookies(cookies);
                log.info("Auth cookies set successfully");
            } else {
                throw new ChatGPTException("Missing required authentication tokens", HttpStatus.UNAUTHORIZED.value());
            }

            page = context.newPage();
            page.setDefaultTimeout(timeout);

            // Add event listeners
            page.onDialog(dialog -> {
                log.warn("Dialog appeared: {}", dialog.message());
                dialog.dismiss();
            });

            page.onConsoleMessage(msg -> {
                if ("error".equals(msg.type().toString())) {
                    log.error("Browser console error: {}", msg.text());
                }
            });

            // Add response error handling
            page.onResponse(response -> {
                if (!response.ok() && response.status() == 403) {
                    log.error("Received 403 response from {}", response.url());
                    throw new ChatGPTException("Authentication failed - check your tokens",
                            HttpStatus.FORBIDDEN.value());
                }
            });

            log.info("Browser context setup complete");
        } catch (Exception e) {
            log.error("Failed to setup browser context: ", e);
            throw new ChatGPTException("Failed to setup browser", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public ChatGPTEvaluateResponse evaluateAnswer(String userAnswer, String question, String prompt) {
        return withRetry(() -> {
            try {
                navigateAndWaitForLoad();
                handleInitialDialogs();

                String evaluationPrompt = formatEvaluationPrompt(question, userAnswer, prompt);

                Locator inputArea = page.locator("textarea#prompt-textarea, div[role='textbox']").first();
                if (!inputArea.isVisible()) {
                    resetBrowserSession();
                    throw new ChatGPTException("Input area not found - page may not be loaded correctly",
                            HttpStatus.SERVICE_UNAVAILABLE.value());
                }

                inputArea.click();
                page.keyboard().insertText(evaluationPrompt);
                log.info("Entered evaluation prompt");

                Locator sendButton = page.locator("button[data-testid='send-button']").first();
                if (!sendButton.isVisible()) {
                    throw new ChatGPTException("Send button not found", HttpStatus.SERVICE_UNAVAILABLE.value());
                }
                sendButton.click();
                log.info("Clicked send button");

                String responseText = waitForResponse();
                if (responseText == null || responseText.isEmpty()) {
                    throw new ChatGPTException("No response received", HttpStatus.SERVICE_UNAVAILABLE.value());
                }

                return parseResponse(responseText);
            } catch (PlaywrightException e) {
                log.error("Playwright error: ", e);
                resetBrowserSession();
                throw new ChatGPTException("Browser automation error: " + e.getMessage(),
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            } catch (Exception e) {
                log.error("Evaluation failed: ", e);
                throw new ChatGPTException(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE.value());
            }
        });
    }

    private void navigateAndWaitForLoad() {
        try {
            page.navigate(CHAT_URL);

            // Wait for initial page load
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Check URL after navigation
            String currentUrl = page.url();
            if (!currentUrl.contains("chat.openai.com")) {
                log.error("Navigation failed - landed on wrong URL: {}", currentUrl);
                resetBrowserSession();
                throw new ChatGPTException("Failed to navigate to ChatGPT",
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            // Wait for content to be interactive
            page.waitForSelector("div[role='main']",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));

            log.info("Successfully navigated to ChatGPT: {}", currentUrl);
        } catch (Exception e) {
            log.error("Navigation failed: {}", e.getMessage());
            resetBrowserSession();
            throw new ChatGPTException("Failed to load ChatGPT page: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    private void handleInitialDialogs() {
        try {
            List<Locator> buttons = page
                    .locator("button:has-text('Next'), button:has-text('Done'), button:has-text('OK')").all();
            for (Locator button : buttons) {
                if (button.isVisible()) {
                    button.click();
                    page.waitForTimeout(1000);
                }
            }
        } catch (Exception e) {
            log.debug("No dialogs to handle: {}", e.getMessage());
        }
    }

    private String waitForResponse() {
        try {
            // First check for error messages
            Locator errorLocator = page.locator("div.text-red-500").first();
            if (errorLocator != null && errorLocator.isVisible()) {
                String errorText = errorLocator.textContent();
                log.error("ChatGPT error message: {}", errorText);
                throw new ChatGPTException("ChatGPT error: " + errorText,
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            // Wait for response with retry logic
            for (int i = 0; i < 3; i++) {
                try {
                    // Wait for response element
                    Locator responseLocator = page.locator("div.markdown p")
                            .filter(new Locator.FilterOptions().setHasText("Score:")).first();

                    // Check if element exists
                    if (responseLocator == null) {
                        log.warn("Response element not found, attempt {}/3", i + 1);
                        page.waitForTimeout(2000);
                        continue;
                    }

                    // Wait for element to be visible and have content
                    responseLocator.waitFor(new Locator.WaitForOptions()
                            .setTimeout(timeout)
                            .setState(WaitForSelectorState.VISIBLE));

                    String responseText = responseLocator.textContent();
                    if (responseText != null && responseText.contains("Score:") &&
                            responseText.contains("Evaluation:") &&
                            responseText.contains("Improvements:")) {
                        return responseText;
                    }

                    log.warn("Invalid response format, retrying...");
                    page.waitForTimeout(2000);
                } catch (TimeoutError e) {
                    if (i < 2) {
                        log.warn("Response timeout, attempt {}/3", i + 1);
                        page.waitForTimeout(2000);
                    } else {
                        log.error("Final timeout waiting for response");
                        resetBrowserSession();
                        throw new ChatGPTException("Timeout waiting for response",
                                HttpStatus.SERVICE_UNAVAILABLE.value());
                    }
                }
            }

            throw new ChatGPTException("Failed to get properly formatted response",
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        } catch (Exception e) {
            log.error("Error waiting for response: ", e);
            resetBrowserSession();
            throw new ChatGPTException("Failed to get response: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    private void resetBrowserSession() {
        try {
            if (page != null) {
                page.close();
            }
            if (context != null) {
                context.close();
            }
            setupBrowserContext();
        } catch (Exception e) {
            log.error("Error resetting browser session: ", e);
        }
    }

    private String formatEvaluationPrompt(String question, String userAnswer, String prompt) {
        return String.format(
                "Please evaluate this answer. Format as follows:\n" +
                        "Score: (0-100)\n" +
                        "Evaluation: (brief)\n" +
                        "Improvements: (list)\n\n" +
                        "Question: %s\n" +
                        "Answer: %s\n" +
                        "Context: %s",
                question, userAnswer, prompt);
    }

    private <T> T withRetry(Supplier<T> operation) {
        Exception lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed: {}", i + 1, e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        String errorMessage = lastException != null ? lastException.getMessage() : "Unknown error";
        throw new ChatGPTException("Operation failed after " + MAX_RETRIES + " attempts: " + errorMessage,
                HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    private ChatGPTEvaluateResponse parseResponse(String responseText) {
        try {
            return ChatGPTEvaluateResponse.builder()
                    .score(parseScore(responseText))
                    .evaluation(parseSection(responseText, "Evaluation:"))
                    .improvements(parseSection(responseText, "Improvements:"))
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse response: ", e);
            throw new ChatGPTException("Failed to parse response", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private double parseScore(String text) {
        try {
            int start = text.indexOf("Score:") + 6;
            int end = text.indexOf("\n", start);
            if (end == -1)
                end = text.length();
            String scoreText = text.substring(start, end).trim();
            return Double.parseDouble(scoreText.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            log.error("Failed to parse score: {}", e.getMessage());
            return 0.0;
        }
    }

    private String parseSection(String text, String sectionHeader) {
        try {
            int start = text.indexOf(sectionHeader) + sectionHeader.length();
            int end = text.indexOf("\n", start);
            if (end == -1)
                end = text.length();
            return text.substring(start, end).trim();
        } catch (Exception e) {
            log.error("Failed to parse section {}: {}", sectionHeader, e.getMessage());
            return "Not available";
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (page != null) {
                page.close();
            }
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            log.warn("Error during cleanup: {}", e.getMessage());
        }
    }
}