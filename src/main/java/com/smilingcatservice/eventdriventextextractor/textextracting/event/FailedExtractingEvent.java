package com.smilingcatservice.eventdriventextextractor.textextracting.event;

public class FailedExtractingEvent extends TextExtractionEvent {
    public FailedExtractingEvent(String processId) {
        super(processId, EventStatus.FAILED_EXTRACTING);
    }

    @Override
    public MessageChannel getChannel() {
        return MessageChannel.FAILED;
    }
}
