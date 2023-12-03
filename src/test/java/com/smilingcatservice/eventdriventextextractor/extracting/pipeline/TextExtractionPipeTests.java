package com.smilingcatservice.eventdriventextextractor.extracting.pipeline;

import com.smilingcatservice.eventdriventextextractor.Config.SseResponseConfig;
import com.smilingcatservice.eventdriventextextractor.Config.SseResponseConverter;
import com.smilingcatservice.eventdriventextextractor.textextracting.controller.TextExtractionController;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.EventStatus;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.MessageChannel;
import com.smilingcatservice.eventdriventextextractor.textextracting.exception.TextExtractionException;
import com.smilingcatservice.eventdriventextextractor.textextracting.executor.TextExtractionTaskService;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe.TextExtractionPipe;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionEventServiceImpl;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionService;
import com.smilingcatservice.eventdriventextextractor.textextracting.sse.SseEmitterConfig;
import com.smilingcatservice.eventdriventextextractor.uploading.strategy.ImageFileType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SseResponseConfig.class, SseEmitterConfig.class, WebMvcAutoConfiguration.class,
        TextExtractionEventServiceImpl.class, TextExtractionPipe.class, TextExtractionController.class})
@AutoConfigureMockMvc
public class TextExtractionPipeTests {
    private final String processId = "f02dcd89-28ef-4ddb-8939-4aaa279ecc61";
    @Value("${app.directory.uploads}")
    private String uploads;
    private TextExtractionDto textExtractionDto = new TextExtractionDto();
    private List<Path> validConvertedFiles = new ArrayList<>();
    private List<Path> invalidConvertedFiles = new ArrayList<>();
    private ImageFileRecord validEngPdf;
    @MockBean
    private TextExtractionService textExtractionService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SseResponseConverter sseResponseConverter;
    @Autowired
    private ConcurrentHashMap<String, SseEmitter> sseEmitters;
    @Autowired
    private TextExtractionPipe textExtractionPipe;

    @BeforeAll
    public void setup() {
        validEngPdf = new ImageFileRecord(
                1L,
                ImageFileType.PDF,
                "converted-image-file.jpg",
                "file-location",
                1);
        validConvertedFiles.add(Path.of(uploads,
                validEngPdf.fileLocation(), validEngPdf.originalFilename()));
        invalidConvertedFiles.add(Path.of(uploads,
                "wrong-file-location", validEngPdf.originalFilename()));
        textExtractionDto.setImageFileRecord(validEngPdf);
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
    }

    @Test
    public void givenWrongFileLocation_whenTextExtracting_thenThrowsTextExtractionExceptionCauseIOException() throws Exception {
        textExtractionDto.setConvertedFiles(invalidConvertedFiles);
        String expectedMessage = "ImageIO failed to load the image";
        MvcResult mvcResult = mockMvc.perform(get("/text-extraction/emitter/{process-id}", processId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        Exception exception = assertThrows(
                TextExtractionException.class,
                () -> textExtractionPipe.process(textExtractionDto));

        assertEquals(exception.getMessage(), expectedMessage);
        assertTrue(exception.getCause() instanceof IOException);

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM))
                .andExpect(serverResponse -> {
                    sseResponseConverter.readValue(serverResponse.getResponse().getContentAsString());
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.FAILED),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.FAILED_EXTRACTING),
                            () -> assertTrue(sseResponseConverter.getData().result().isEmpty())
                    );
                })
                .andDo(print());
    }

    @Test
    public void givenValidFile_whenTextExtracting_thenReturnExtractedText() throws Exception {
        textExtractionDto.setConvertedFiles(validConvertedFiles);
        textExtractionDto.setLang("eng");
        SseEmitter sseEmitter = sseEmitters.get(processId);
        TextExtractionDto result;

        MvcResult mvcResult = mockMvc.perform(get("/text-extraction/emitter/{process-id}", processId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        result = textExtractionPipe.process(textExtractionDto);

        assertTrue(result.getExtractedPages().size() > 0);
        assertTrue(result.getExtractedPages().get(0).size() > 0);

        sseEmitter.complete();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM))
                .andExpect(serverResponse -> {
                    sseResponseConverter.readValue(serverResponse.getResponse().getContentAsString());
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.SUCCESSFUL),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.EXTRACTED),
                            () -> assertTrue(sseResponseConverter.getData().result().isEmpty())
                    );
                })
                .andDo(print());
    }
}
