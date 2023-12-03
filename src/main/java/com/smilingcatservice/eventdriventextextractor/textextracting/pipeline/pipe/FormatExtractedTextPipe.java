package com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe;

import com.smilingcatservice.eventdriventextextractor.textextracting.event.CompletedFormattingEvent;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.Pipe;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.ImageFileStorageService;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionEventService;
import lombok.AllArgsConstructor;
import net.sourceforge.tess4j.Word;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FormatExtractedTextPipe implements Pipe {
    private final ImageFileStorageService imageFileStorageService;
    private final TextExtractionEventService eventPublisher;
    @Override
    public TextExtractionDto process(TextExtractionDto input) {
        List<String> formattedText = new ArrayList<>();

        for (List<Word> line : input.getExtractedPages()) {
            StringBuilder stringBuilder = new StringBuilder();

            for (Word word : line) {
                stringBuilder.append(word.getText());
            }

            formattedText.add(stringBuilder.toString());
        }

        input.setFormattedText(formattedText);
        eventPublisher.publishEvent(new CompletedFormattingEvent(input.getProcessId(), input.getFormattedText().get(0)));

        imageFileStorageService.saveExtractedText(formattedText, input.getImageFileRecord());

        return input;
    }
}
