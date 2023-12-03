package com.smilingcatservice.eventdriventextextractor.textextracting.event;

public abstract class TextExtractionEvent {
    private final String DEFAULT_RESULT = "";
    private final String processId;
    private final EventData data;

    TextExtractionEvent(String processId, EventStatus status) {
        this.processId = processId;
        this.data = new EventData(status, DEFAULT_RESULT);
    }

    TextExtractionEvent(String processId, EventStatus status, String result) {
        this.processId = processId;
        this.data = new EventData(status, result);
    }

    public String getProcessId() {
        return processId;
    }
    public EventData getData() {return data;}

    abstract public MessageChannel getChannel();
}
