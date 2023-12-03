package com.smilingcatservice.eventdriventextextractor.textextracting.service;

import com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord;

import java.util.List;

public interface ImageFileStorageService {
    void deleteTempDirectory(ImageFileRecord imageFileRecord);
    void saveExtractedText(List<String> extractedText, ImageFileRecord imageFileRecord);
}
