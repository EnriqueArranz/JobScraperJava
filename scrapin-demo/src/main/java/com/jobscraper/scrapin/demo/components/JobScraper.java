package com.jobscraper.scrapin.demo.components;

import com.jobscraper.scrapin.demo.Job;
import com.jobscraper.scrapin.demo.Utilities;
import com.jobscraper.scrapin.demo.interfaces.JobSite;
import com.jobscraper.scrapin.demo.sites.LinkedInSite;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class JobScraper {
    @Autowired
    private LinkedInSite linkedInSite; // Inject the LinkedInSite bean
    @Value("${job.search.keyword}")
    private String searchKeyword;
    @Value("${job.search.location}")
    private String searchLocation;
    @Value("${job.csv.path}")
    private String csvFilePath;
    @Value("${webdriver.chrome.driver}")
    private String chromeDriverPath;
    private List<JobSite> jobSites;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private int applicationCount = 0;
    @Autowired
    public JobScraper(@Value("${job.search.keyword}") String searchKeyword,
                      @Value("${job.search.location}") String searchLocation,
                      @Value("${job.csv.path}") String csvFilePath,
                      @Value("${webdriver.chrome.driver}") String chromeDriverPath) {
        this.searchKeyword = searchKeyword;
        this.searchLocation = searchLocation;
        this.csvFilePath = csvFilePath;
        this.chromeDriverPath = chromeDriverPath;
    }

    public void scrapeJobs() {
        List<Job> allJobs = new ArrayList<>();
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        WebDriver driver = new ChromeDriver();
        Utilities utilities = new Utilities();
        try {
            linkedInSite.login(driver);
            String baseUrl = linkedInSite.getSearchUrl(searchKeyword, searchLocation);
            int resultsPerPage = 25;

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            // Navigate to the initial search URL
            driver.get(baseUrl);
            // Wait for the search results to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".jobs-search-results-list")));
            Thread.sleep(2000);
            // Extract jobs using the updated method
            List<Job> notFilteredJobs = linkedInSite.extractJobs(driver, baseUrl, resultsPerPage);
            // filter
            allJobs.addAll(linkedInSite.filterJobsByJava(notFilteredJobs));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        utilities.writeToCsv(allJobs,csvFilePath);
    }



//    public void applyToJobs() throws InterruptedException {
//        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
//        WebDriver driver = new ChromeDriver();
//        LinkedInSite linkedinSite = new LinkedInSite();
//        linkedinSite.login(driver);
//        int applicationCount = 0;
//        List<String> filteredJobs = getLinksFromCSV(csvFilePath);
//        // Apply to jobs
//        for (String jobUrl : filteredJobs) {
//            driver.get(jobUrl);
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
//            new WebDriverWait(driver, Duration.ofSeconds(30)).until(
//                    webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
//            );
//            Thread.sleep(2000);
//            WebElement firstApplyButton = wait.until(ExpectedConditions.elementToBeClickable(
//                    By.id("ember39")
//            ));
//
//
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//            js.executeScript("arguments[0].click()", firstApplyButton);
//            Thread.sleep(2000);
//            firstApplyButton.click();
//            try {
//                Thread.sleep(2000);
//                WebElement sendButton = wait.until(ExpectedConditions.elementToBeClickable(
//                        By.id("ember323")
//                ));
//                if (sendButton.isDisplayed()) {
//                    Thread.sleep(2000);
//                    sendButton.click();
//                    applicationCount++;
//                }
//            } catch (TimeoutException e) {
//                // Si no aparece 'Enviar solicitud', intentar encontrar el botón 'Ir al siguiente paso'
//                boolean nextStep = true;
//                while (nextStep) {
//                    try {
//                        Thread.sleep(2000);
//                        WebElement nextStepButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[span[text()='Ir al siguiente paso']]")));
//                        if (nextStepButton.isDisplayed()) {
//                            Thread.sleep(2000);
//                            nextStepButton.click();
//                        }
//                    } catch (TimeoutException e2) {
//                        nextStep = false; // Salir del ciclo si no se encuentra el botón
//                    }
//                    try {
//                        Thread.sleep(2000);
//                        WebElement reviewButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[span[text()='Revisar']]")));
//                        if (reviewButton.isDisplayed()) {
//                            reviewButton.click();
//                            // Ahora buscar el botón 'Enviar solicitud' después de hacer clic en 'Revisar'
//                            try {
//                                Thread.sleep(2000);
//                                WebElement sendButtonAfterReview = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[span[text()='Enviar solicitud']]")));
//                                if (sendButtonAfterReview.isDisplayed()) {
//                                    Thread.sleep(2000);
//                                    sendButtonAfterReview.click();
//                                    applicationCount++;
//                                }
//                            } catch (TimeoutException e3) {
//                                // No se encontró el botón 'Enviar solicitud' después de 'Revisar'
//                                System.out.println("Botón 'Enviar solicitud' no encontrado tras hacer clic en 'Revisar'.");
//                            }
//                            nextStep = false; // Si encontramos el botón de revisar, salimos del ciclo
//                        }
//                    } catch (TimeoutException e4) {
//                        // No ha aparecido el botón 'Revisar', continuamos buscando el botón 'Ir al siguiente paso'
//                        nextStep = true;
//                    }
//                }
//            }
//        }
//        System.out.println("Se aplicaron " + applicationCount + " solicitudes.");
//    }

    public void applyToJobs() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        Utilities utilities = new Utilities();
        linkedInSite.login(driver);
        int applicationCount = 0;
        List<String> filteredJobs = utilities.getLinksFromCSV(csvFilePath);

        for (String jobUrl : filteredJobs) {
            try {
                applyToJob(jobUrl);
            } catch (Exception e) {
                System.out.println("Failed to apply for job at URL: " + jobUrl);
                e.printStackTrace();
            }
        }
        System.out.println("Se aplicaron " + applicationCount + " solicitudes.");
    }

    private void applyToJob(String jobUrl) throws InterruptedException {
        driver.get(jobUrl);
        waitForPageLoad();

        clickApplyButton();

        if (isSimpleApplication()) {
            handleSimpleApplication();
        } else {
            handleComplexApplication();
        }
    }

    private void waitForPageLoad() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));
        sleep(4000);
    }

    private void clickApplyButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        WebElement button = driver.findElement(By.xpath("//button[contains(@class, 'jobs-apply-button') and contains(@class, 'artdeco-button') and contains(@class, 'artdeco-button--3') and contains(@class, 'artdeco-button--primary')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        sleep(2000);
    }

    private boolean isSimpleApplication() {
        try {
            WebElement sendButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@aria-label, 'Enviar solicitud')]")));
            return sendButton.isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void handleSimpleApplication() {
        WebElement sendButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@aria-label, 'Enviar solicitud')]")));
        sendButton.click();
        applicationCount++;
    }

    private void handleComplexApplication() {
        while (true) {
            if (clickIfPresent("//button[span[text()='Ir al siguiente paso']]")) {
                continue;
            }
            if (clickIfPresent("//button[span[text()='Revisar']]")) {
                if (clickIfPresent("//button[span[text()='Enviar solicitud']]")) {
                    applicationCount++;
                    break;
                }
            }
            break;
        }
    }

    private boolean clickIfPresent(String xpath) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            if (element.isDisplayed()) {
                element.click();
                sleep(2000);
                return true;
            }
        } catch (TimeoutException e) {
            // Element not found or not clickable
        }
        return false;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}



