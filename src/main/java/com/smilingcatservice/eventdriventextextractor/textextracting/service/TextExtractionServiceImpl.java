package com.smilingcatservice.eventdriventextextractor.textextracting.service;

import com.smilingcatservice.eventdriventextextractor.textextracting.executor.TextExtractionTaskService;
import com.smilingcatservice.eventdriventextextractor.textextracting.executor.TextExtractionTask;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.Pipeline;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TextExtractionServiceImpl implements TextExtractionService {
    private final ImageFileService imageFileService;
    private final Pipeline pipeline;
    private final TextExtractionTaskService textExtractionTaskService;

    @Override
    public void extractText(long imageFileId, String processId, String lang) {
        TextExtractionDto textExtractionDto = new TextExtractionDto();
        textExtractionDto.setImageFileRecord(imageFileService.getImageFileRecord(imageFileId));
        textExtractionDto.setProcessId(processId);
        textExtractionDto.setLang(lang);

        textExtractionTaskService.runTask(new TextExtractionTask(pipeline, textExtractionDto));
    }
}
