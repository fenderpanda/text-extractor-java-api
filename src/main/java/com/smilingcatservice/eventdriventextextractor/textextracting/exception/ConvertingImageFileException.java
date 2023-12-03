package com.smilingcatservice.eventdriventextextractor.textextracting.exception;

public class ConvertingImageFileException extends RuntimeException {
    public ConvertingImageFileException(String message) { super(message); }
    public ConvertingImageFileException(String message, Throwable ex) {
        super(message, ex);
    }
}
