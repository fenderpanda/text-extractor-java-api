package com.smilingcatservice.eventdriventextextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class EventDrivenTextExtractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventDrivenTextExtractorApplication.class, args);
    }

}
