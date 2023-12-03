package com.smilingcatservice.eventdriventextextractor.textextracting.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

@Slf4j
public class TextExtractionThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);

        thread.setUncaughtExceptionHandler(
                (t, e) -> log.error("UNCAUGHT in thread " + t.getName(), e)
        );

        return thread;
    }
}
