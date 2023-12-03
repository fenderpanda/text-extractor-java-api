package com.smilingcatservice.eventdriventextextractor.textextracting.repository;

import com.smilingcatservice.eventdriventextextractor.uploading.entity.ImageFile;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ImageFileRepository extends JpaRepository<ImageFile, Long> {
    @Query("""
        select new com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord(
            i.id,
            i.type,
            i.originalFilename,
            i.fileLocation,
            i.pageAmount
        )
        from ImageFile i
        where i.id = :imageFileId
        """)
    Optional<ImageFileRecord> findImageFileRecordById(@Param("imageFileId") long imageFileId);
}
