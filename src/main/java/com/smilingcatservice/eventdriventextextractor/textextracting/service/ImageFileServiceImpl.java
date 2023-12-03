package com.smilingcatservice.eventdriventextextractor.textextracting.service;

import com.smilingcatservice.eventdriventextextractor.textextracting.exception.ImageFileRecordNotFoundException;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord;
import com.smilingcatservice.eventdriventextextractor.textextracting.repository.ImageFileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ImageFileServiceImpl implements ImageFileService {
    private final ImageFileRepository imageFileRepository;

    @Override
    public ImageFileRecord getImageFileRecord(long imageFileId) {
        return imageFileRepository.findImageFileRecordById(imageFileId)
                .orElseThrow(() -> new ImageFileRecordNotFoundException(
                        "Image file not found with id: " + imageFileId
                ));
    }
}
