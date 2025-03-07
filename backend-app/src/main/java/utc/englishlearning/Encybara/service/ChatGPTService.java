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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.response.chatgpt.ChatGPTEvaluateResponse;
import utc.englishlearning.Encybara.exception.ChatGPTException;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class ChatGPTService {

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

                // Add user agent
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
                throw new ChatGPTException("Failed to initialize browser: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }

    public ChatGPTEvaluateResponse evaluateAnswer(String userAnswer, String question, String prompt) {
        try {
            initializeWebDriver();
            driver.get("https://chat.openai.com/");
            log.info("Navigated to ChatGPT");

            // Wait for initial page load
            Thread.sleep(3000);

            // Look for "Next" button or other elements that might appear initially
            try {
                List<WebElement> buttons = driver.findElements(By.xpath(
                        "//button[contains(text(), 'Next') or contains(text(), 'Done') or contains(text(), 'OK')]"));
                for (WebElement button : buttons) {
                    if (button.isDisplayed()) {
                        button.click();
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                log.debug("No initial dialogs to handle");
            }

            // Wait for and find the input textarea
            WebElement inputArea = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(
                            "//textarea[contains(@placeholder, 'Send a message') or contains(@id, 'prompt-textarea')]")));

            String evaluationPrompt = String.format(
                    "Please evaluate this answer to the following question. Follow this format exactly:\n" +
                            "Score: (give a score from 0-100)\n" +
                            "Evaluation: (brief evaluation of the answer)\n" +
                            "Improvements: (suggestions for improvement)\n\n" +
                            "Question: %s\n" +
                            "User's Answer: %s\n" +
                            "Additional Context/Prompt: %s",
                    question, userAnswer, prompt);

            inputArea.sendKeys(evaluationPrompt);
            log.info("Entered evaluation prompt");

            // Find and click send button
            WebElement sendButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-testid='send-button' or contains(@class, 'send')]")));
            sendButton.click();
            log.info("Clicked send button");

            // Wait for response
            WebElement responseElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(@class, 'markdown')]//p[contains(text(), 'Score:')]")));

            // Wait a bit more to ensure full response is loaded
            Thread.sleep(2000);

            String responseText = responseElement.getText();
            log.info("Received response text");

            double score = parseScore(responseText);
            String evaluation = parseSection(responseText, "Evaluation:");
            String improvements = parseSection(responseText, "Improvements:");

            return ChatGPTEvaluateResponse.builder()
                    .score(score)
                    .evaluation(evaluation)
                    .improvements(improvements)
                    .build();

        } catch (Exception e) {
            log.error("Failed to evaluate answer: ", e);
            throw new ChatGPTException("Failed to evaluate answer: " + e.getMessage(),
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
            throw new ChatGPTException("Failed to parse score from response",
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
            throw new ChatGPTException("Failed to parse " + sectionHeader + " from response",
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