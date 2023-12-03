package com.smilingcatservice.eventdriventextextractor.uploading.controller;

import com.smilingcatservice.eventdriventextextractor.uploading.payload.ImageFileRecord;
import com.smilingcatservice.eventdriventextextractor.uploading.service.UploadImageFileService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin({"http://localhost:3000", "https://smilingcatservice.com"})
@AllArgsConstructor
public class UploadFileController {
    private final UploadImageFileService uploadImageFileService;

    @PostMapping("/upload")
    public ImageFileRecord uploadImageFile(@RequestParam(value = "image-file") MultipartFile imageFile) {
        return uploadImageFileService.upload(imageFile);
    }
}
