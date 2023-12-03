package com.smilingcatservice.eventdriventextextractor.textextracting.service;

import com.smilingcatservice.eventdriventextextractor.textextracting.exception.SaveExtractedTextException;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord;
import com.smilingcatservice.eventdriventextextractor.textextracting.repository.ImageFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageFileStorageServiceImpl implements ImageFileStorageService {
    @Value("${app.directory.uploads}")
    private String uploads;
    private final ImageFileRepository imageFileRepository;

    @Override
    public void deleteTempDirectory(ImageFileRecord imageFileRecord) {
        Path pathToBeDeleted = Path.of(uploads, imageFileRecord.fileLocation());

        try(Stream<Path> files = Files.walk(pathToBeDeleted)) {
            files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

            imageFileRepository.deleteById(imageFileRecord.id());
        } catch (IOException e) {
            log.error("Couldn't delete directory '" + imageFileRecord.fileLocation() + "'", e);
        }
    }

    @Override
    public void saveExtractedText(List<String> extractedText, ImageFileRecord imageFileRecord) {
        String outputFile = getOutputFile(imageFileRecord.originalFilename());

        try (PrintWriter writer = new PrintWriter(Path.of(
                uploads,
                imageFileRecord.fileLocation(),
                outputFile).toFile())) {
            for (String str : extractedText) {
                    writer.print(str);
            }
        } catch (IOException e) {
            throw new SaveExtractedTextException("Failed to create '" + outputFile + "'", e);
        }
    }

    private String getOutputFile(String originalFilename) {
        return originalFilename.split("\\.")[0] + ".txt";
    }
}
