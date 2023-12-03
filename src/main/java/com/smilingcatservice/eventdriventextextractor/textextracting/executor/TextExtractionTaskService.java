package com.smilingcatservice.eventdriventextextractor.textextracting.executor;

import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
@AllArgsConstructor
public class TextExtractionTaskService {
    private final ThreadPoolExecutor textExtractionExecutor;

    public void runTask(Runnable task) {
        textExtractionExecutor.execute(task);
    }
    public ArrayBlockingQueue<Runnable> getAwaitingTasks() {
        return (ArrayBlockingQueue<Runnable>) textExtractionExecutor.getQueue();
    }

    @PreDestroy
    public void destroy() {
        textExtractionExecutor.shutdown();
    }
}
