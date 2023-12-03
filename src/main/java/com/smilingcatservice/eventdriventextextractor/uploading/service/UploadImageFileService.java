package com.smilingcatservice.eventdriventextextractor.uploading.service;

import com.smilingcatservice.eventdriventextextractor.uploading.payload.ImageFileRecord;
import org.springframework.web.multipart.MultipartFile;

public interface UploadImageFileService {
    ImageFileRecord upload(MultipartFile multipartFile);
}
