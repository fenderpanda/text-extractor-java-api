package com.smilingcatservice.eventdriventextextractor.uploading.exception;

public class CreateTempDirectoryException extends RuntimeException {
    public CreateTempDirectoryException(String message, Throwable ex) {
        super(message, ex);
    }
}
