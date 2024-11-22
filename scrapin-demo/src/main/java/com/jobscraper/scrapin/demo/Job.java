package com.jobscraper.scrapin.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Job {
    private String title;
    private String company;
    private String location;
    private String link;
    private String source;

}