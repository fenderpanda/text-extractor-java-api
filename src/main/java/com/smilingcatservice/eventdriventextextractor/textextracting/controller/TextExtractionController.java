package com.smilingcatservice.eventdriventextextractor.textextracting.controller;

import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@AllArgsConstructor
@CrossOrigin({"http://localhost:3000", "https://smilingcatservice.com"})
@RequestMapping("/text-extraction")
public class TextExtractionController {
    private final ConcurrentHashMap<String, SseEmitter> sseEmitters;
    private final TextExtractionService textExtractionService;

    @GetMapping("/image-file/{image-file-id}/lang/{lang}")
    public String extractText(
            @PathVariable(value = "image-file-id") long imageFileId,
            @PathVariable String lang) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        String processId = UUID.randomUUID().toString();
        sseEmitter.onCompletion(() -> sseEmitters.remove(processId));
        sseEmitter.onError((throwable) -> {
            throwable.printStackTrace();
            sseEmitters.remove(processId);
        });
        sseEmitters.put(processId, sseEmitter);

        textExtractionService.extractText(imageFileId, processId, lang);

        return processId;
    }
    @GetMapping(value = "/emitter/{process-id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> getEmitter(@PathVariable(name = "process-id") String processId) {
        /*
        We should add these headers to turn off nginx buffering and make sse working in realtime
         */
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "text/event-stream");
        responseHeaders.set("Connection", "keep-alive");
        responseHeaders.set("Cache-Control", "no-cache");
        responseHeaders.set("X-Accel-Buffering", "no");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(sseEmitters.get(processId));
    }
}
