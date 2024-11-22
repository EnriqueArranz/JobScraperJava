package com.jobscraper.scrapin.demo.interfaces;

import com.jobscraper.scrapin.demo.Job;
import org.openqa.selenium.WebDriver;

import java.util.List;

public interface JobSite {
    String getSearchUrl(String keyword, String location);

    List<Job> extractJobs(WebDriver driver, String baseUrl, int resultsPerPage);
    void login(WebDriver driver);
}
