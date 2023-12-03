package com.smilingcatservice.eventdriventextextractor.textextracting.event;

public class CompletedExtractingEvent extends TextExtractionEvent {
    public CompletedExtractingEvent(String processId) {
        super(processId, EventStatus.EXTRACTED);
    }

    @Override
    public MessageChannel getChannel() {
        return MessageChannel.SUCCESSFUL;
    }
}
