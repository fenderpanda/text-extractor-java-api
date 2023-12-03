package com.smilingcatservice.eventdriventextextractor.uploading.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class UploadFileExceptionController {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnsupportedImageFileException.class)
    public String handleUnsupportedImageFileException(UnsupportedImageFileException e) {
        log.info(e.getMessage(), e);

        return "Unsupported image file";
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ImageFileSizeException.class)
    public String handleMaxUploadSizeExceededException(ImageFileSizeException e) {
        log.info(e.getMessage(), e);

        return "Max upload file exceeded";
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PageAmountException.class)
    public String handlePageAmountException(PageAmountException e) {
        log.info(e.getMessage(), e);

        return "Your file contains more 1 page";
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(CreateTempDirectoryException.class)
    public String handleCreateTempDirectoryException(CreateTempDirectoryException e) {
        log.error(e.getMessage(), e);

        return "Internal server error";
    }
}
