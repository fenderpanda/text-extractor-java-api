package com.smilingcatservice.eventdriventextextractor.textextracting.exception;

public class TextExtractionException extends RuntimeException {
    public TextExtractionException(String message, Throwable ex) {
        super(message, ex);
    }
}
