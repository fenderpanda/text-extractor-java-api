package com.smilingcatservice.eventdriventextextractor.textextracting.pipeline;

import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe.ConvertingImageFilePipe;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe.FormatExtractedTextPipe;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe.TextExtractionPipe;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.*;

import java.util.ArrayList;

@Configuration
@AllArgsConstructor
public class PipelineConfig {
    private final ConvertingImageFilePipe convertingImageFilePipe;
    private final TextExtractionPipe textExtractionPipe;
    private final FormatExtractedTextPipe formatExtractedTextPipe;

    @Bean
    public Pipeline pipeline() {
        ArrayList<Pipe> pipes = new ArrayList<>();
        pipes.add(convertingImageFilePipe);
        pipes.add(textExtractionPipe);
        pipes.add(formatExtractedTextPipe);

        return new Pipeline(pipes);
    }
}
