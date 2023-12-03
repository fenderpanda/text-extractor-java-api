package com.smilingcatservice.eventdriventextextractor.textextracting.event;

public class CompletedConvertingEvent extends TextExtractionEvent {
    public CompletedConvertingEvent(String processId) {
        super(processId, EventStatus.CONVERTED);
    }

    @Override
    public MessageChannel getChannel() {
        return MessageChannel.SUCCESSFUL;
    }
}
