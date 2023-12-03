package com.smilingcatservice.eventdriventextextractor.textextracting.service;

import com.smilingcatservice.eventdriventextextractor.textextracting.event.TextExtractionEvent;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class TextExtractionEventServiceImpl implements TextExtractionEventService {
    private final ConcurrentHashMap<String, SseEmitter> sseEmitters;

    @Override
    public void publishEvent(TextExtractionEvent event) {
        String processId = event.getProcessId();
        SseEmitter emitter = sseEmitters.get(processId);

        try {
            emitter.send(SseEmitter.event()
                    .name(event.getChannel().toString())
                    .data(event.getData(), MediaType.APPLICATION_JSON));

            if (event.getData().status().isLastEvent()) {
                emitter.complete();
                sseEmitters.remove(processId);
            }
        } catch (IOException e) {
            emitter.completeWithError(e);
            sseEmitters.remove(processId);
        }
    }
}
