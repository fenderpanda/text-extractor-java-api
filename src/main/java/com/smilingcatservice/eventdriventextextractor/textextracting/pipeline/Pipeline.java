package com.smilingcatservice.eventdriventextextractor.textextracting.pipeline;

import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;

import java.util.List;

public class Pipeline {
    private final List<Pipe> pipes;

    public Pipeline(List<Pipe> pipes) {
        this.pipes = pipes;
    }
    public void execute(TextExtractionDto input) {
        TextExtractionDto output = input;

        for (Pipe pipe : pipes) {
            output = pipe.process(output);
        }
    }
}
