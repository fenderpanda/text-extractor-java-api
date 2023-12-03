package com.smilingcatservice.eventdriventextextractor.textextracting.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.concurrent.RejectedExecutionException;

@Slf4j
@ControllerAdvice
public class TextExtractionExceptionController {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ImageFileRecordNotFoundException.class)
    public String handleImageFileRecordNotFoundException(ImageFileRecordNotFoundException e) {
        log.info("Image file not found", e);

        return "Something went wrong";
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RejectedExecutionException.class)
    public String handleRejectedExecutionException(RejectedExecutionException e) {
        return "Server is busy. Please try again later";
    }
}
