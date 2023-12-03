package com.smilingcatservice.eventdriventextextractor.Config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SseResponseConfig {
    @Bean
    public SseResponseConverter sseResponseConverter() {
        return new SseResponseConverter();
    }
}
