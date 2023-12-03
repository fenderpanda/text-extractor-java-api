package com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe;

import com.smilingcatservice.eventdriventextextractor.textextracting.event.*;
import com.smilingcatservice.eventdriventextextractor.textextracting.exception.TextExtractionException;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.Pipe;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionEventService;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class TextExtractionPipe implements Pipe {
    private final String tessdata;
    private final TextExtractionEventService eventPublisher;

    public TextExtractionPipe(
            @Value("${app.directory.tessdata}") String tessdata,
            TextExtractionEventService eventPublisher
    ) {
        this.tessdata = tessdata;
        this.eventPublisher = eventPublisher;
    }
    @Override
    public TextExtractionDto process(TextExtractionDto input) {
        List<List<Word>> extractedPages = new ArrayList<>();

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdata);
        tesseract.setVariable("user_defined_dpi", "300");
        tesseract.setLanguage(input.getLang());
        tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO_OSD);
        tesseract.setOcrEngineMode(1);

        BufferedImage image;
        for (Path path : input.getConvertedFiles()) {
            try {
                image = ImageIO.read(path.toFile());
                List<Word> lines = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
                extractedPages.add(lines);
            } catch (IOException e) {
                eventPublisher.publishEvent(new FailedExtractingEvent(input.getProcessId()));
                throw new TextExtractionException("ImageIO failed to load the image", e);
            }
        }

        eventPublisher.publishEvent(new CompletedExtractingEvent(input.getProcessId()));

        input.setExtractedPages(extractedPages);

        return input;
    }
}
