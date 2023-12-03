package com.smilingcatservice.eventdriventextextractor.textextracting.executor;

import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.Pipeline;

public class TextExtractionTask implements Runnable {
    private final Pipeline pipeline;
    private final TextExtractionDto textExtractionDto;

    public TextExtractionTask(Pipeline pipeline, TextExtractionDto textExtractionDto) {
        this.pipeline = pipeline;
        this.textExtractionDto = textExtractionDto;
    }

    public Long getImageFileId() {
        return textExtractionDto.getImageFileRecord().id();
    }

    @Override
    public void run() {
        pipeline.execute(textExtractionDto);
    }
}
