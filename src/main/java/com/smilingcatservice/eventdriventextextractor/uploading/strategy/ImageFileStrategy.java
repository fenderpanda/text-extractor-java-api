package com.smilingcatservice.eventdriventextextractor.uploading.strategy;

import java.nio.file.Path;

public interface ImageFileStrategy {
    ImageFileType getType();
    default int getPageAmount(Path path) {
        return 1;
    }
}
