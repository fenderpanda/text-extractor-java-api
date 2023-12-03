package com.smilingcatservice.eventdriventextextractor.uploading.repository;

import com.smilingcatservice.eventdriventextextractor.uploading.entity.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedImageFileRepository extends JpaRepository<ImageFile, Long> {
}
