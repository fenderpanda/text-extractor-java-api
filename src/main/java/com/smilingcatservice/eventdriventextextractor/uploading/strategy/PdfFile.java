package com.smilingcatservice.eventdriventextextractor.uploading.strategy;

import net.sourceforge.tess4j.util.PdfBoxUtilities;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class PdfFile implements ImageFileStrategy {
    @Override
    public ImageFileType getType() {
        return ImageFileType.PDF;
    }

    @Override
    public int getPageAmount(Path path) {

        return PdfBoxUtilities.getPdfPageCount(path.toFile());
    }
}
