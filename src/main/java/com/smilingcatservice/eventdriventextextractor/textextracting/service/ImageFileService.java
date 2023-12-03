package com.smilingcatservice.eventdriventextextractor.textextracting.service;

import com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord;

public interface ImageFileService {
    ImageFileRecord getImageFileRecord(long imageFileId);
}
