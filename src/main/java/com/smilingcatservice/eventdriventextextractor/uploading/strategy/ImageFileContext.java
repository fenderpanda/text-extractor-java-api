package com.smilingcatservice.eventdriventextextractor.uploading.strategy;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Component
@AllArgsConstructor
public class ImageFileContext {
    private final Map<ImageFileType, ImageFileStrategy> imageFileStrategyMap;

    public int getPageAmount(ImageFileType type, Path path) {

        return imageFileStrategyMap.get(type).getPageAmount(path);
    }
}
