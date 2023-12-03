package com.smilingcatservice.eventdriventextextractor.extracting.repository;

import com.smilingcatservice.eventdriventextextractor.textextracting.repository.ImageFileRepository;
import com.smilingcatservice.eventdriventextextractor.uploading.entity.ImageFile;
import com.smilingcatservice.eventdriventextextractor.uploading.strategy.ImageFileType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ImageFileRepositoryTests {
    private ImageFile testImageFile;
    @Autowired
    private ImageFileRepository imageFileRepository;

    @BeforeAll
    public void setup() {
        testImageFile = new ImageFile();
        testImageFile.setType(ImageFileType.PDF);
        testImageFile.setOriginalFilename("valid-pdf.pdf");
        testImageFile.setFileLocation("f02dcd89-28ef-4ddb-8939-4aaa279ecc61-location");
        testImageFile.setPageAmount(1);
    }

    @Test
    public void givenImageFile_whenFindImageFileRecordById_thenOk() {
        long id = imageFileRepository.save(testImageFile).getId();

        imageFileRepository.findImageFileRecordById(id).ifPresentOrElse(
                imr -> assertEquals("valid-pdf.pdf", imr.originalFilename()),
                () -> fail("Image file record not found"));
    }
}
