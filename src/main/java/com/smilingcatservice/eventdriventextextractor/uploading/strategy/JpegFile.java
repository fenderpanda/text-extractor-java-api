package com.smilingcatservice.eventdriventextextractor.uploading.strategy;

import org.springframework.stereotype.Component;

@Component
public class JpegFile implements ImageFileStrategy {
    @Override
    public ImageFileType getType() {
        return ImageFileType.JPEG;
    }
}
