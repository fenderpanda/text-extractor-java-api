package com.smilingcatservice.eventdriventextextractor.Config;

import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe.ConvertingImageFilePipe;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionEventService;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionEventServiceImpl;
import com.smilingcatservice.eventdriventextextractor.textextracting.sse.SseEmitterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration
@ComponentScan(basePackageClasses = {SseEmitterConfig.class})
public class ConvertingImageFilePipeConfig {
    @Value("${app.image-magick.command}")
    String commandMagick;
    @Value("${app.image-magick.execution-time-limit}")
    int executionTimeLimit;
    @Value("${app.directory.uploads}")
    String uploads;
    @Autowired
    private ConcurrentHashMap<String, SseEmitter> sseEmitters;
    @Bean
    public TextExtractionEventService textExtractionEventService() {
        return new TextExtractionEventServiceImpl(sseEmitters);
    }

    @Bean
    public ConvertingImageFilePipe validPipe() {
        return new ConvertingImageFilePipe(commandMagick, executionTimeLimit, uploads, textExtractionEventService());
    }

    @Bean
    public ConvertingImageFilePipe pipeWithSmallExecutionTime() {
        return new ConvertingImageFilePipe(commandMagick, 2, uploads, textExtractionEventService());
    }
}
