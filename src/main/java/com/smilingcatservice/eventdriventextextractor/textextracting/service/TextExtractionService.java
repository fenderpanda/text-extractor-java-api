package com.smilingcatservice.eventdriventextextractor.textextracting.service;

import java.util.List;

public interface TextExtractionService {
    void extractText(long imageFileId, String processId, String lang);
}
