package com.smilingcatservice.eventdriventextextractor.uploading.strategy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ImageFileConfig.class, ImageFileContext.class,
        JpegFile.class, JpgFile.class, HeicFile.class, PngFile.class, PdfFile.class},
        initializers = ConfigDataApplicationContextInitializer.class)
public class ImageFileContextTests {
    @Value("${app.directory.uploads}")
    private String uploads;
    @Autowired
    ImageFileContext imageFileContext;
    private Path defaultPath, pathToOnePagePdf, pathToEightPagesPdf;

    @BeforeAll
    public void setup() {
        defaultPath = Path.of("");
        pathToOnePagePdf = Path.of(uploads, "one-page-eng.pdf");
        pathToEightPagesPdf = Path.of(uploads, "eight-pages.pdf");
    }

    @Test
    void givenJpgFile_whenGetPageAmount_thenReturnOnePage() {
        int pageAmount = imageFileContext.getPageAmount(ImageFileType.JPG, defaultPath);

        assertEquals(pageAmount, 1);
    }

    @Test
    void givenJpegFile_whenGetPageAmount_thenReturnOnePage() {
        int pageAmount = imageFileContext.getPageAmount(ImageFileType.JPEG, defaultPath);

        assertEquals(pageAmount, 1);
    }

    @Test
    void givenPngFile_whenGetPageAmount_thenReturnOnePage() {
        int pageAmount = imageFileContext.getPageAmount(ImageFileType.PNG, defaultPath);

        assertEquals(pageAmount, 1);
    }

    @Test
    void givenPdfFile_whenGetPageAmount_thenReturnOnePage() {
        int pageAmount = imageFileContext.getPageAmount(ImageFileType.PDF, pathToOnePagePdf);

        assertEquals(pageAmount, 1);
    }

    @Test
    void givenPdfFile_whenGetPageAmount_thenReturnEightPages() {
        int pageAmount = imageFileContext.getPageAmount(ImageFileType.PDF, pathToEightPagesPdf);

        assertEquals(pageAmount, 8);
    }
}
