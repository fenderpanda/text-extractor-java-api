package com.smilingcatservice.eventdriventextextractor.Config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.EventData;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.MessageChannel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SseResponseConverter {
    private MessageChannel event;
    private EventData data;

    public void readValue(String value) throws JsonProcessingException {
        String[] lines = value.split("\n");
        String event = lines[0].replace("event:", "");
        String data = lines[1].replace("data:", "");

        ObjectMapper mapper = new ObjectMapper();
        setEvent(MessageChannel.valueOf(event));
        setData(mapper.readValue(data, EventData.class));
    }
}
