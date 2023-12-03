package com.smilingcatservice.eventdriventextextractor.uploading.service;

import com.smilingcatservice.eventdriventextextractor.uploading.entity.ImageFile;
import com.smilingcatservice.eventdriventextextractor.uploading.exception.CreateTempDirectoryException;
import com.smilingcatservice.eventdriventextextractor.uploading.exception.ImageFileSizeException;
import com.smilingcatservice.eventdriventextextractor.uploading.exception.PageAmountException;
import com.smilingcatservice.eventdriventextextractor.uploading.exception.UnsupportedImageFileException;
import com.smilingcatservice.eventdriventextextractor.uploading.payload.ImageFileRecord;
import com.smilingcatservice.eventdriventextextractor.uploading.repository.UploadedImageFileRepository;
import com.smilingcatservice.eventdriventextextractor.uploading.strategy.ImageFileContext;
import com.smilingcatservice.eventdriventextextractor.uploading.strategy.ImageFileType;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Service
public class UploadImageFileServiceImpl implements UploadImageFileService {
    private final long MAX_FILESIZE = DataSize.ofMegabytes(10).toBytes();
    private final int MAX_PAGE_AMOUNT = 1;
    private final String uploads;
    private final UploadedImageFileRepository imageFileRepository;
    private final ImageFileContext imageFileContext;

    public UploadImageFileServiceImpl(
            @Value("${app.directory.uploads}") String uploads,
            UploadedImageFileRepository imageFileRepository,
            ImageFileContext imageFileContext
    ) {
        this.uploads = uploads;
        this.imageFileRepository = imageFileRepository;
        this.imageFileContext = imageFileContext;
    }

    @Override
    public ImageFileRecord upload(MultipartFile multipartFile) {
        if (multipartFile.getSize() > MAX_FILESIZE) {
            throw new ImageFileSizeException(
                    "Size of '" + multipartFile.getOriginalFilename() + "': " + multipartFile.getSize()
            );
        }

        String originalFilename = getOriginalFilename(multipartFile);
        ImageFileType type = getType(originalFilename);
        String fileLocation = getUuidFileLocation(originalFilename);

        Path pathToSavedFile = saveImageFile(multipartFile, fileLocation, originalFilename);
        int pageAmount = imageFileContext.getPageAmount(type, pathToSavedFile);

        if (pageAmount > MAX_PAGE_AMOUNT) {
            throw new PageAmountException(
                    "Pages of '" + originalFilename + "': " + pageAmount
            );
        }

        ImageFile newImageFile = new ImageFile();
        newImageFile.setType(type);
        newImageFile.setOriginalFilename(originalFilename);
        newImageFile.setFileLocation(fileLocation);
        newImageFile.setPageAmount(pageAmount);

        long imageFileId = imageFileRepository.save(newImageFile).getId();

        return new ImageFileRecord(imageFileId, type, originalFilename, fileLocation, pageAmount);
    }

    private String getOriginalFilename(MultipartFile multipartFile) {
        Optional<String> originalFilename = Optional.ofNullable(multipartFile.getOriginalFilename());

        if (originalFilename.isPresent())
            return originalFilename.get().replace( " ", "_");

        throw new UnsupportedImageFileException("Unsupported image file '" + originalFilename + "'");
    }

    private String getUuidFileLocation(String originalFilename) {
        return UUID.randomUUID() + "-" + originalFilename.split("\\.")[0];
    }

    private ImageFileType getType(String originalFilename) {
        Optional<String> extension = Optional.ofNullable(FilenameUtils.getExtension(originalFilename));

        if (extension.isPresent()) {
            for (ImageFileType type : ImageFileType.values()) {
                if (type.toString().equalsIgnoreCase(extension.get()))
                    return type;
            }
        }

        throw new UnsupportedImageFileException("Unsupported image file '" + originalFilename + "'");
    }

    private Path saveImageFile(MultipartFile multipartFile, String fileLocation, String uuidFilename) {
        Path newDirectoryPath = Path.of(uploads, fileLocation);

        try {
            Files.createDirectory(newDirectoryPath);
            Path savedFilePath = Path.of(newDirectoryPath.toString(), uuidFilename);
            multipartFile.transferTo(savedFilePath);

            return savedFilePath;
        } catch (IOException e) {
            throw new CreateTempDirectoryException("Failed to create directory: " + newDirectoryPath, e);
        }
    }
}
