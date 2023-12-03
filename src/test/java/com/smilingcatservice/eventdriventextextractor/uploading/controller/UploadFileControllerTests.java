package com.smilingcatservice.eventdriventextextractor.uploading.controller;

import com.smilingcatservice.eventdriventextextractor.textextracting.repository.ImageFileRepository;
import com.smilingcatservice.eventdriventextextractor.uploading.exception.ImageFileSizeException;
import com.smilingcatservice.eventdriventextextractor.uploading.exception.PageAmountException;
import com.smilingcatservice.eventdriventextextractor.uploading.exception.UnsupportedImageFileException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
public class UploadFileControllerTests {
    @Value("${app.directory.uploads}")
    private String uploads;
    private Path pathToWrongPageAmountPdf;
    private MockMultipartFile fileSizeExceededFile;
    private MockMultipartFile unsupportedFile;
    private MockMultipartFile validPngFile;
    @Autowired
    private ImageFileRepository imageFileRepository;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() {
        final String REQUEST_PARAM_NAME = "image-file";

        fileSizeExceededFile = new MockMultipartFile(
                REQUEST_PARAM_NAME,
                "test-file.png",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                new byte[1024 * 1024 * 11]
        );
        unsupportedFile = new MockMultipartFile(
                REQUEST_PARAM_NAME,
                "test-file.exe",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                new byte[1024 * 1024]
        );
        validPngFile = new MockMultipartFile(
                REQUEST_PARAM_NAME,
                "valid-file.png",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                new byte[1024 * 1024 * 4]
        );

        pathToWrongPageAmountPdf = Path.of(uploads, "eight-pages.pdf");
    }

    @AfterAll
    public void cleanup() throws Exception {
        imageFileRepository.deleteAll();

        Path pathToUploads = Path.of(uploads);
        List<Path> result;

        try (Stream<Path> walk = Files.walk(pathToUploads)) {
            result = walk
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().contains("valid-file") ||
                            p.getFileName().toString().contains("wrong-page-amount"))
                    .toList();
        }
        for (Path deleteDirectory : result) {
            try(Stream<Path> walk = Files.walk(deleteDirectory)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    @Test
    public void givenLargeFile_whenUpload_thenThrowsImageFileSizeException() throws Exception {
        mockMvc.perform(multipart("/upload").file(fileSizeExceededFile))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ImageFileSizeException))
                .andDo(print());
    }

    @Test
    public void givenUnsupportedFile_whenUpload_thenThrowsUnsupportedImageFileException() throws Exception {
        mockMvc.perform(multipart("/upload").file(unsupportedFile))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnsupportedImageFileException))
                .andDo(print());
    }

    @Test
    public void givenWrongPageAmountPdf_whenUpload_thenThrowsPageAmountException() throws Exception {
        MockMultipartFile wrongPageAmountFile = new MockMultipartFile(
                "image-file",
                "wrong-page-amount.pdf",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                Files.readAllBytes(pathToWrongPageAmountPdf)
        );

        mockMvc.perform(multipart("/upload").file(wrongPageAmountFile))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PageAmountException))
                .andDo(print());
    }

    @Test
    public void givenValidPngFile_whenUpload_thenOk() throws Exception {
        mockMvc.perform(multipart("/upload").file(validPngFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value("PNG"))
                .andExpect(jsonPath("$.originalFilename", is(validPngFile.getOriginalFilename())))
                .andExpect(jsonPath("$.fileLocation").isString())
                .andExpect(jsonPath("$.pageAmount", is(1)))
                .andDo(print());
    }
}
