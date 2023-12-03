package com.smilingcatservice.eventdriventextextractor.textextracting.payload;

import com.smilingcatservice.eventdriventextextractor.uploading.strategy.ImageFileType;

public record ImageFileRecord(
    long id,
    ImageFileType type,
    String originalFilename,
    String fileLocation,
    int pageAmount
) {}
