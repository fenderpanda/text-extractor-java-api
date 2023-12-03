package com.smilingcatservice.eventdriventextextractor.textextracting.pipeline;

import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;

public interface Pipe {
    TextExtractionDto process(TextExtractionDto input);

}
