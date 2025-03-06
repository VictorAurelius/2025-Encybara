package utc.englishlearning.Encybara.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexityEvaluateResponse;
import utc.englishlearning.Encybara.exception.PerplexityException;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
public class PerplexityService {

    @Value("${perplexity.email}")
    private String perplexityEmail;

    @Value("${perplexity.session.timeout.hours:168}") // Default to 1 week if not specified
    private long sessionTimeoutHours;

    private WebDriver driver;
    private WebDriverWait wait;
    private boolean isAwaitingAuthCode = false;
    private Instant lastLoginTime;

    private void initializeWebDriver() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080"); // Set larger window size
            options.addArguments("--start-maximized");

            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Increase timeout
        }
    }

    public boolean initiateLogin() {
        try {
            initializeWebDriver();
            driver.get("https://www.perplexity.ai/");
            log.info("Navigated to Perplexity.ai");

            // Wait for page load
            Thread.sleep(2000);

            // Try multiple selectors for login button
            WebElement loginButton = null;
            String[] loginSelectors = {
                    "//button[contains(text(), 'Log In')]",
                    "//button[contains(text(), 'Login')]",
                    "//button[contains(@class, 'login')]",
                    "//div[contains(@class, 'login')]",
                    "//a[contains(text(), 'Sign in')]"
            };

            for (String selector : loginSelectors) {
                try {
                    loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                    log.info("Found login button using selector: {}", selector);
                    break;
                } catch (Exception e) {
                    log.debug("Selector not found: {}", selector);
                }
            }

            if (loginButton == null) {
                throw new PerplexityException("Could not find login button with any known selector",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            loginButton.click();
            log.info("Clicked login button");

            // Find and fill email field
            WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@type='email' or contains(@placeholder, 'email')]")));
            emailInput.clear();
            emailInput.sendKeys(perplexityEmail);
            log.info("Entered email address");

            // Click continue/submit - try multiple selectors
            String[] submitSelectors = {
                    "//button[@type='submit']",
                    "//button[contains(text(), 'Continue')]",
                    "//button[contains(text(), 'Next')]",
                    "//button[contains(@class, 'submit')]"
            };

            WebElement submitButton = null;
            for (String selector : submitSelectors) {
                try {
                    submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                    log.info("Found submit button using selector: {}", selector);
                    break;
                } catch (Exception e) {
                    log.debug("Submit button selector not found: {}", selector);
                }
            }

            if (submitButton == null) {
                throw new PerplexityException("Could not find submit button with any known selector",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            submitButton.click();
            log.info("Clicked submit button");

            // Wait for auth code input field - try multiple selectors
            String[] codeInputSelectors = {
                    "//input[@type='text' and contains(@placeholder, 'code')]",
                    "//input[contains(@placeholder, 'verification')]",
                    "//input[contains(@placeholder, 'OTP')]"
            };

            WebElement codeInput = null;
            for (String selector : codeInputSelectors) {
                try {
                    codeInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    log.info("Found code input field using selector: {}", selector);
                    break;
                } catch (Exception e) {
                    log.debug("Code input selector not found: {}", selector);
                }
            }

            if (codeInput == null) {
                throw new PerplexityException("Could not find verification code input field",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            isAwaitingAuthCode = true;
            log.info("Successfully initiated login process");
            return true;
        } catch (Exception e) {
            log.error("Failed to initiate Perplexity login: ", e);
            throw new PerplexityException("Failed to initiate login: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public boolean submitAuthCode(String authCode) {
        if (!isAwaitingAuthCode) {
            throw new PerplexityException("No login process in progress. Please initiate login first.",
                    HttpStatus.BAD_REQUEST.value());
        }

        try {
            log.info("Submitting authentication code");
            Thread.sleep(1000); // Wait for animation

            // Try multiple selectors for code input
            String[] codeInputSelectors = {
                    "//input[@type='text' and contains(@placeholder, 'code')]",
                    "//input[contains(@placeholder, 'verification')]",
                    "//input[contains(@placeholder, 'OTP')]",
                    "//input[contains(@placeholder, 'enter code')]",
                    "//input[contains(@placeholder, 'magic')]"
            };

            WebElement codeInput = null;
            for (String selector : codeInputSelectors) {
                try {
                    codeInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    log.info("Found code input field using selector: {}", selector);
                    break;
                } catch (Exception e) {
                    log.debug("Code input selector not found: {}", selector);
                }
            }

            if (codeInput == null) {
                throw new PerplexityException("Could not find code input field",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            codeInput.clear();
            codeInput.sendKeys(authCode);
            log.info("Entered authentication code");

            // Try multiple selectors for submit button
            String[] submitSelectors = {
                    "//button[@type='submit']",
                    "//button[contains(text(), 'Verify')]",
                    "//button[contains(text(), 'Submit')]",
                    "//button[contains(text(), 'Continue')]",
                    "//button[contains(@class, 'submit')]"
            };

            WebElement submitButton = null;
            for (String selector : submitSelectors) {
                try {
                    submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                    log.info("Found submit button using selector: {}", selector);
                    break;
                } catch (Exception e) {
                    log.debug("Submit button selector not found: {}", selector);
                }
            }

            if (submitButton == null) {
                throw new PerplexityException("Could not find submit button", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            submitButton.click();
            log.info("Clicked submit button");

            // Try multiple selectors for successful login confirmation
            String[] successSelectors = {
                    "//div[contains(@class, 'user-menu')]",
                    "//div[contains(@class, 'profile')]",
                    "//div[contains(@class, 'avatar')]",
                    "//button[contains(@aria-label, 'User menu')]",
                    "//div[contains(@class, 'account')]"
            };

            WebElement userMenu = null;
            for (String selector : successSelectors) {
                try {
                    userMenu = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    log.info("Found user menu using selector: {}", selector);
                    break;
                } catch (Exception e) {
                    log.debug("User menu selector not found: {}", selector);
                }
            }

            if (userMenu == null) {
                throw new PerplexityException("Could not confirm successful login",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            isAwaitingAuthCode = false;
            lastLoginTime = Instant.now();
            log.info("Successfully authenticated with code");
            return true;
        } catch (Exception e) {
            log.error("Failed to submit auth code: ", e);
            throw new PerplexityException("Failed to submit auth code: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public PerplexityEvaluateResponse evaluateAnswer(String userAnswer, String question, String prompt) {
        try {
            if (driver == null || !isLoggedIn()) {
                throw new PerplexityException("Not logged in or session expired. Please authenticate again.",
                        HttpStatus.UNAUTHORIZED.value());
            }

            driver.get("https://www.perplexity.ai/");

            try {
                WebElement newThreadButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(), 'New Thread') or contains(@aria-label, 'New Thread')]")));
                newThreadButton.click();
            } catch (Exception e) {
                log.debug("New thread button not found, continuing with current thread");
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

            WebElement inputField = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(
                            "//textarea[contains(@placeholder, 'Ask anything...') or contains(@placeholder, 'Message Perplexity')]")));
            inputField.sendKeys(evaluationPrompt);

            WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@type='submit' or contains(@aria-label, 'Send')]")));
            submitButton.click();

            WebElement response = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(@class, 'response') or contains(@class, 'answer')]//p")));

            String responseText = response.getText();

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

    private boolean isLoggedIn() {
        try {
            if (lastLoginTime == null) {
                return false;
            }

            // Check if session has expired
            Duration sessionAge = Duration.between(lastLoginTime, Instant.now());
            if (sessionAge.toHours() >= sessionTimeoutHours) {
                log.info("Session expired after {} hours (timeout: {} hours)", sessionAge.toHours(),
                        sessionTimeoutHours);
                return false;
            }

            // Check if user is still logged in via UI element
            driver.findElement(By.xpath("//div[contains(@class, 'user-menu') or contains(@class, 'profile')]"));
            return true;
        } catch (Exception e) {
            return false;
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
            isAwaitingAuthCode = false;
            lastLoginTime = null;
        }
    }
}