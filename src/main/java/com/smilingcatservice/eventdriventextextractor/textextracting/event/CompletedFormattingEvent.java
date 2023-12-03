package com.smilingcatservice.eventdriventextextractor.textextracting.event;

public class CompletedFormattingEvent extends TextExtractionEvent {
    public CompletedFormattingEvent(String processId, String result) {
        super(processId, EventStatus.FORMATTED, result);
    }

    @Override
    public MessageChannel getChannel() {
        return MessageChannel.SUCCESSFUL;
    }
}
