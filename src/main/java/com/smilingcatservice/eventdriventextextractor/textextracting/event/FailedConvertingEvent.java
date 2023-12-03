package com.smilingcatservice.eventdriventextextractor.textextracting.event;

public class FailedConvertingEvent extends TextExtractionEvent {
    public FailedConvertingEvent(String processId) {
        super(processId, EventStatus.FAILED_CONVERTING);
    }

    @Override
    public MessageChannel getChannel() {
        return MessageChannel.FAILED;
    }
}
