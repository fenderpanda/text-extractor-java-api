package com.smilingcatservice.eventdriventextextractor.extracting.pipeline;

import com.smilingcatservice.eventdriventextextractor.Config.SseResponseConfig;
import com.smilingcatservice.eventdriventextextractor.Config.SseResponseConverter;
import com.smilingcatservice.eventdriventextextractor.textextracting.controller.TextExtractionController;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.EventStatus;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.MessageChannel;
import com.smilingcatservice.eventdriventextextractor.textextracting.executor.TextExtractionTaskService;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe.FormatExtractedTextPipe;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.ImageFileStorageServiceImpl;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionEventServiceImpl;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionService;
import com.smilingcatservice.eventdriventextextractor.textextracting.sse.SseEmitterConfig;
import net.sourceforge.tess4j.Word;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SseEmitterConfig.class, SseResponseConfig.class, WebMvcAutoConfiguration.class,
        TextExtractionController.class, TextExtractionEventServiceImpl.class, FormatExtractedTextPipe.class})
@AutoConfigureMockMvc
public class FormatExtractedTextPipeTests {
    private final String processId = "f02dcd89-28ef-4ddb-8939-4aaa279ecc61";
    private final List<List<Word>> extractedPages = new ArrayList<>();
    private final TextExtractionDto textExtractionDto = new TextExtractionDto();
    @MockBean
    private ImageFileStorageServiceImpl imageFileStorageService;
    @MockBean
    private TextExtractionService textExtractionService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SseResponseConverter sseResponseConverter;
    @Autowired
    private ConcurrentHashMap<String, SseEmitter> sseEmitters;
    @Autowired
    private FormatExtractedTextPipe formatExtractedTextPipe;

    @BeforeAll
    public void setup() {
        List<Word> words = new ArrayList<>();

        for (int i=0; i<100; i++) {
            words.add(new Word(
                    "Some text",
                    97.456f,
                    new Rectangle(1, 1, 1, 1)
            ));
        }

        extractedPages.add(words);
    }

    @BeforeEach
    public void prepareTestCase() {
        sseEmitters.clear();
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onCompletion(() -> sseEmitters.remove(processId));
        sseEmitter.onError((throwable) -> {
            throwable.printStackTrace();
            sseEmitters.remove(processId);
        });
        sseEmitters.put(processId, sseEmitter);

        textExtractionDto.setProcessId(processId);
        textExtractionDto.setExtractedPages(extractedPages);
    }

    @Test
    public void givenExtractedPages_whenFormat_thenReturnFormattedText() throws Exception {
        TextExtractionDto result;

        MvcResult mvcResult = mockMvc.perform(get("/text-extraction/emitter/{process-id}", processId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        result = formatExtractedTextPipe.process(textExtractionDto);

        assertTrue(result.getFormattedText().size() > 0);
        result.getFormattedText().forEach(text -> {
            assertNotNull(text);
            assertFalse(text.isEmpty());
        });

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM))
                .andExpect(serverResponse -> {
                    sseResponseConverter.readValue(serverResponse.getResponse().getContentAsString());
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.SUCCESSFUL),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.FORMATTED),
                            () -> assertFalse(sseResponseConverter.getData().result().isEmpty())
                    );
                })
                .andDo(print());
    }
}
