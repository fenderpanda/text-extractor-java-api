package com.smilingcatservice.eventdriventextextractor.textextracting.service;

import com.smilingcatservice.eventdriventextextractor.textextracting.event.TextExtractionEvent;

public interface TextExtractionEventService {
    void publishEvent(TextExtractionEvent event);
}
