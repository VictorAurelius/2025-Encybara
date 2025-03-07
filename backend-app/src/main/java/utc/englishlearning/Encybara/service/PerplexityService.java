package utc.englishlearning.Encybara.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexityEvaluateResponse;
import utc.englishlearning.Encybara.exception.PerplexityException;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class PerplexityService {

    private WebDriver driver;
    private WebDriverWait wait;

    private void initializeWebDriver() {
        if (driver == null) {
            try {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();

                // Basic options for headless operation
                options.addArguments("--headless=new");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");

                // Window size and display settings
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--start-maximized");

                // Performance and stability options
                options.addArguments("--disable-extensions");
                options.addArguments("--disable-popup-blocking");
                options.addArguments("--disable-notifications");

                // Add user agent to avoid detection
                options.addArguments(
                        "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

                driver = new ChromeDriver(options);

                // Configure page load timeouts
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

                // Initialize explicit wait with longer timeout
                wait = new WebDriverWait(driver, Duration.ofSeconds(45));

                log.info("WebDriver initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize WebDriver: ", e);
                throw new PerplexityException("Failed to initialize browser: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }

    public PerplexityEvaluateResponse evaluateAnswer(String userAnswer, String question, String prompt) {
        try {
            initializeWebDriver();
            driver.get("https://www.perplexity.ai/");
            log.info("Navigated to Perplexity.ai");

            // Wait for initial page load and any modals/popups to appear
            Thread.sleep(5000);

            // Check and close any modals or popups that might interfere
            try {
                List<WebElement> closeButtons = driver.findElements(
                        By.xpath("//button[contains(@aria-label, 'close') or contains(@class, 'close')]"));
                for (WebElement closeButton : closeButtons) {
                    if (closeButton.isDisplayed()) {
                        closeButton.click();
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                log.debug("No modals/popups found to close");
            }

            // Look for input field with expanded selectors
            WebElement inputField = null;
            String[] inputSelectors = {
                    "//textarea[contains(@placeholder, 'Ask anything')]",
                    "//textarea[contains(@placeholder, 'Message Perplexity')]",
                    "//textarea[contains(@class, 'text-input')]",
                    "//textarea[@role='textbox']",
                    "//div[contains(@class, 'input')]//textarea"
            };

            for (String selector : inputSelectors) {
                try {
                    inputField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    if (inputField.isDisplayed()) {
                        log.info("Found input field using selector: {}", selector);
                        break;
                    }
                } catch (Exception e) {
                    log.debug("Input field selector not found: {}", selector);
                }
            }

            if (inputField == null) {
                throw new PerplexityException("Could not find input field",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            String evaluationPrompt = String.format(
                    "Please evaluate this answer to the following question. Follow this format exactly:\n" +
                            "Score: (give a score from 0-100)\n" +
                            "Evaluation: (brief evaluation of the answer)\n" +
                            "Improvements: (suggestions for improvement)\n\n" +
                            "Question: %s\n" +
                            "User's Answer: %s\n" +
                            "Additional Context/Prompt: %s",
                    question, userAnswer, prompt);

            inputField.sendKeys(evaluationPrompt);
            log.info("Entered evaluation prompt");

            // Wait a moment for any dynamic UI updates
            Thread.sleep(2000);

            // Find and click the submit button using various selectors
            WebElement submitButton = null;
            String[] submitSelectors = {
                    "//button[@type='submit']",
                    "//button[contains(@aria-label, 'Send')]",
                    "//button[contains(@aria-label, 'Submit')]",
                    "//button[contains(@class, 'send')]",
                    "//button[.//svg[contains(@class, 'send')]]",
                    "//button[contains(@class, 'submit')]",
                    "//div[contains(@class, 'send')]//button",
                    "//button[.//*[local-name()='svg']]", // Look for any button containing an SVG
                    "//textarea/..//button", // Button near textarea
                    "//textarea/following::button[1]" // First button after textarea
            };

            for (String selector : submitSelectors) {
                try {
                    log.debug("Trying submit button selector: {}", selector);
                    List<WebElement> buttons = driver.findElements(By.xpath(selector));
                    for (WebElement button : buttons) {
                        if (button.isDisplayed() && button.isEnabled()) {
                            submitButton = button;
                            log.info("Found submit button using selector: {}", selector);
                            break;
                        }
                    }
                    if (submitButton != null)
                        break;
                } catch (Exception e) {
                    log.debug("Submit button selector not found: {}", selector);
                }
            }

            if (submitButton == null) {
                // Try JavaScript click on the last visible button as a fallback
                try {
                    List<WebElement> allButtons = driver.findElements(By.tagName("button"));
                    for (int i = allButtons.size() - 1; i >= 0; i--) {
                        WebElement button = allButtons.get(i);
                        if (button.isDisplayed() && button.isEnabled()) {
                            submitButton = button;
                            log.info("Found potential submit button using fallback method");
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Fallback submit button search failed: {}", e.getMessage());
                }
            }

            if (submitButton == null) {
                throw new PerplexityException("Could not find submit button",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            // Try multiple click methods
            boolean clicked = false;
            try {
                submitButton.click();
                clicked = true;
            } catch (Exception e) {
                log.debug("Regular click failed: {}", e.getMessage());
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
                    clicked = true;
                } catch (Exception je) {
                    log.debug("JavaScript click failed: {}", je.getMessage());
                }
            }

            if (!clicked) {
                throw new PerplexityException("Failed to click submit button",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            log.info("Clicked submit button");

            // Wait for and get the response with improved selectors
            WebElement response = null;
            String[] responseSelectors = {
                    "//div[contains(@class, 'response')]//p",
                    "//div[contains(@class, 'answer')]//p",
                    "//div[contains(@class, 'message')]//p",
                    "//div[contains(@class, 'response')]",
                    "//div[contains(@class, 'answer')]",
                    "//div[contains(@role, 'presentation')]//p"
            };

            for (String selector : responseSelectors) {
                try {
                    response = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    if (response.isDisplayed() && !response.getText().trim().isEmpty()) {
                        log.info("Found response using selector: {}", selector);
                        break;
                    }
                } catch (Exception e) {
                    log.debug("Response selector not found: {}", selector);
                }
            }

            if (response == null) {
                throw new PerplexityException("Could not find response text",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            String responseText = response.getText();
            log.info("Received response text");

            double score = parseScore(responseText);
            String evaluation = parseSection(responseText, "Evaluation:");
            String improvements = parseSection(responseText, "Improvements:");

            return PerplexityEvaluateResponse.builder()
                    .score(score)
                    .evaluation(evaluation)
                    .improvements(improvements)
                    .build();

        } catch (Exception e) {
            log.error("Failed to evaluate answer: ", e);
            throw new PerplexityException("Failed to evaluate answer: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private double parseScore(String text) {
        try {
            int start = text.indexOf("Score:") + 6;
            int end = text.indexOf("\n", start);
            String scoreText = text.substring(start, end).trim();
            return Double.parseDouble(scoreText.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            throw new PerplexityException("Failed to parse score from response",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
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
            throw new PerplexityException("Failed to parse " + sectionHeader + " from response",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public void cleanup() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}