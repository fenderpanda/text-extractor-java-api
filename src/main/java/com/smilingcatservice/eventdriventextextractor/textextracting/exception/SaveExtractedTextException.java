package com.smilingcatservice.eventdriventextextractor.textextracting.exception;

public class SaveExtractedTextException extends RuntimeException {
    public SaveExtractedTextException(String message, Throwable ex) {
        super(message, ex);
    }
}
