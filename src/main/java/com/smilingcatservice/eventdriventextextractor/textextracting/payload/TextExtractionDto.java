package com.smilingcatservice.eventdriventextextractor.textextracting.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sourceforge.tess4j.Word;

import java.nio.file.Path;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class TextExtractionDto {
    private String processId;
    private String lang;
    private ImageFileRecord imageFileRecord;
    private List<Path> convertedFiles;
    private List<List<Word>> extractedPages;
    private List<String> formattedText;
}
