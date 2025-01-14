package com.jobscraper.scrapin.demo;

import com.jobscraper.scrapin.demo.components.JobScraper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class ScrapinDemoApplication {

    @Autowired
    private JobScraper jobScraper;

    public static void main(String[] args) {
        SpringApplication.run(ScrapinDemoApplication.class, args);
    }

@PostConstruct
    public void init() {
      //jobScraper.scrapeJobs();
      jobScraper.applyToJobs();
    }
}