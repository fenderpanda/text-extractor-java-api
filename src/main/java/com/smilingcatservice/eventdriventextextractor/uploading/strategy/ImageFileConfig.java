package com.smilingcatservice.eventdriventextextractor.uploading.strategy;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class ImageFileConfig {
    private final List<ImageFileStrategy> imageFileStrategies;

    @Bean
    public Map<ImageFileType, ImageFileStrategy> imageFileStrategyMap() {
        Map<ImageFileType, ImageFileStrategy> map = new EnumMap<>(ImageFileType.class);
        imageFileStrategies.forEach(
                imageFileStrategy -> map.put(imageFileStrategy.getType(), imageFileStrategy));

        return map;
    }
}
