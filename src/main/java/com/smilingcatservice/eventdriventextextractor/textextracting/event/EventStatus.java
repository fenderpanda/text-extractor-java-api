package com.smilingcatservice.eventdriventextextractor.textextracting.event;

public enum EventStatus {
    CONVERTED {public boolean isLastEvent() {return false;}},
    EXTRACTED {public boolean isLastEvent() {return false;}},
    FORMATTED, FAILED_CONVERTING, FAILED_EXTRACTING;
    public boolean isLastEvent() {
        return true;
    }
}
