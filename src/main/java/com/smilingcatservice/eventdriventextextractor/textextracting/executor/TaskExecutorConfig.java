package com.smilingcatservice.eventdriventextextractor.textextracting.executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class TaskExecutorConfig {
    private final int corePoolSize;
    private final int maximumPoolSize;
    private final int blockingQueueCapacity;

    public TaskExecutorConfig(
            @Value("${app.executor.corePoolSize}") int corePoolSize,
            @Value("${app.executor.maximumPoolSize}") int maximumPoolSize,
            @Value("${app.executor.blockingQueueCapacity}") int blockingQueueCapacity
    ) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.blockingQueueCapacity = blockingQueueCapacity;
    }

    @Bean
    public ThreadPoolExecutor textExtractionExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(blockingQueueCapacity),
                new TextExtractionThreadFactory()
        );
    }
}
