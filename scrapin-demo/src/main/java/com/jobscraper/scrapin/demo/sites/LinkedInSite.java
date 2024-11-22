package com.jobscraper.scrapin.demo.sites;

import com.jobscraper.scrapin.demo.Job;
import com.jobscraper.scrapin.demo.interfaces.JobSite;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class LinkedInSite implements JobSite {
    @Value("${linkedin.username}")
    private String username;
    @Value("${linkedin.password}")
    private String password;
    @Value("${job.search.keyword}")
    private String searchKeyword;
    @Value("${job.search.location}")
    private String searchLocation;

    @Override
    public String getSearchUrl(String searchKeyword, String searchLocation) {
        try {
            String encodedKeyword = URLEncoder.encode(searchKeyword, "UTF-8");
            String encodedLocation = URLEncoder.encode(searchLocation, "UTF-8");
            return "https://www.linkedin.com/jobs/search?keywords=" + encodedKeyword + "&location=" + encodedLocation;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error encoding the URL parameters");
        }
    }

    @Override
    public void login(WebDriver driver) {
        // Hardcoded credentials
        String username = "enrique.arrantzale@gmail.com"; // Replace with your actual username
        String password = "Formentera69"; // Replace with your actual password
        driver.get("https://www.linkedin.com/login");
        driver.manage().window().maximize();
        // Wait for elements to be present
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // Find username and password fields and log in
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
        // Log the credentials (for debugging purposes, remove in production)
        System.out.println("Logging in with username: " + username);
        // Simulate typing the credentials and clicking the login button
        emailField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
    }

    public List<Job> extractJobs(WebDriver driver, String baseUrl, int resultsPerPage) {
        List<Job> jobs = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Maximize the browser window
        driver.manage().window().maximize();

        String jobItemSelector = ".artdeco-entity-lockup__content";
        int startIndex = 0;
        boolean hasMoreJobs = true;

        try {
            while (hasMoreJobs) {
                // Construct the URL for the current page
                String currentPageUrl;
                if (startIndex == 0) {
                    currentPageUrl = baseUrl;
                } else {
                    currentPageUrl = baseUrl + "&start=" + startIndex;
                }
                driver.get(currentPageUrl);

                // Wait for the job list to load
                WebElement scrollableContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".jobs-search-results-list")));
                Thread.sleep(1000);
                // Scroll to load all jobs on the current page
                scrollToLoadAllJobs(js, scrollableContainer);

                // Extract jobs from the current page
                List<Job> pageJobs = extractJobsFromCurrentPage(scrollableContainer, jobItemSelector);
                jobs.addAll(pageJobs);

                System.out.println("Extracted " + pageJobs.size() + " jobs from page starting at index " + startIndex);

                // Check if we should move to the next page
                if (pageJobs.size() < resultsPerPage) {
                    hasMoreJobs = false;
                } else {
                    startIndex += resultsPerPage;
                }
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout waiting for items: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Close the driver to release resources
            if (driver != null) {
                driver.quit();
                System.out.println("Driver closed.");
            }
        }

        System.out.println("Total jobs extracted: " + jobs.size());
        return jobs;
    }

    public List<Job> filterJobsByJava(List<Job> jobs) {
        List<Job> javaJobs = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getTitle().toLowerCase().contains("java")) {
                javaJobs.add(job);
            }
        }
        System.out.println("Found " + javaJobs.size() + " jobs with Java in the title.");
        return javaJobs;
    }


    private void scrollToLoadAllJobs(JavascriptExecutor js, WebElement scrollableContainer) throws InterruptedException {
        int scrollIncrement = 500;
        int previousHeight = 0;
        int currentHeight;
        boolean isMoreJobs = true;
        int attempts = 0;
        while (isMoreJobs && attempts < 20) {
            js.executeScript("arguments[0].scrollBy(0, " + scrollIncrement + ");", scrollableContainer);
            Thread.sleep(1000);
            currentHeight = Integer.parseInt(js.executeScript("return arguments[0].scrollHeight", scrollableContainer)
                    .toString());
            if (currentHeight == previousHeight) {
                isMoreJobs = false;
            } else {
                previousHeight = currentHeight;
                attempts = 0;
            }
            attempts++;
        }
    }

    private List<Job> extractJobsFromCurrentPage(WebElement scrollableContainer, String jobItemSelector) {
        List<Job> pageJobs = new ArrayList<>();
        List<WebElement> items = scrollableContainer.findElements(By.cssSelector(jobItemSelector));
        System.out.println("Found " + items.size() + " items on this page.");

        for (WebElement item : items) {
            try {
                WebElement titleElement = item.findElement(By.cssSelector(".job-card-list__title"));
                String title = titleElement.getText().trim();

                WebElement companyElement = item.findElement(By.cssSelector(".artdeco-entity-lockup__subtitle"));
                String company = companyElement.getText().trim();

                WebElement locationElement = item.findElement(By.cssSelector(".job-card-container__metadata-item"));
                String location = locationElement.getText().trim();

                WebElement linkElement = item.findElement(By.cssSelector("a[data-control-id]"));
                String link = linkElement.getAttribute("href");

                String source = "LinkedIn";

                if (title != null && company != null && location != null && link != null) {
                    pageJobs.add(new Job(title, company, location, link, source));
                    System.out.println("Added job: " + title + " at " + company + " in " + location);
                } else {
                    System.out.println("Failed to extract all information for a job listing.");
                }
            } catch (Exception e) {
                System.out.println("Error processing item: " + e.getMessage());
            }
        }
        return pageJobs;
    }
}


