package com.smilingcatservice.eventdriventextextractor.extracting.pipeline;

import com.smilingcatservice.eventdriventextextractor.Config.ConvertingImageFilePipeConfig;
import com.smilingcatservice.eventdriventextextractor.Config.SseResponseConfig;
import com.smilingcatservice.eventdriventextextractor.Config.SseResponseConverter;
import com.smilingcatservice.eventdriventextextractor.textextracting.controller.TextExtractionController;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.EventStatus;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.MessageChannel;
import com.smilingcatservice.eventdriventextextractor.textextracting.exception.ConvertingImageFileException;
import com.smilingcatservice.eventdriventextextractor.textextracting.executor.TextExtractionTaskService;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe.ConvertingImageFilePipe;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {
        SseEmitterConfig.class, TextExtractionController.class,
        WebMvcAutoConfiguration.class, ConvertingImageFilePipeConfig.class, SseResponseConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ConvertingImageFilePipeTests {
    private final String processId = "f02dcd89-28ef-4ddb-8939-4aaa279ecc61";
    @Value("${app.directory.uploads}")
    private String uploads;
    private final TextExtractionDto textExtractionDto = new TextExtractionDto();
    private ImageFileRecord validEngPdf, nonExistPdf, invalidFile;
    @MockBean
    private TextExtractionService textExtractionService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SseResponseConverter sseResponseConverter;
    @Autowired
    private ConcurrentHashMap<String, SseEmitter> sseEmitters;
    @Autowired
    private ConvertingImageFilePipe validPipe;
    @Autowired
    private ConvertingImageFilePipe pipeWithSmallExecutionTime;

    @BeforeAll
    public void setup() {
        validEngPdf = new ImageFileRecord(
                1L,
                ImageFileType.PDF,
                "valid-eng.pdf",
                "file-location",
                1);
        nonExistPdf = new ImageFileRecord(
                1L,
                ImageFileType.PDF,
                "non-exist-file.pdf",
                "non-exist-location",
                1);
        invalidFile = new ImageFileRecord(
                1L,
                ImageFileType.PDF,
                "invalid-file.exe",
                "file-location",
                1);
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
    public void givenNonExistPdf_whenProcess_thenThrowsConvertingImageFileExceptionWithCauseIOException() throws Exception {
        textExtractionDto.setImageFileRecord(nonExistPdf);
        String expectedMessage = "Failed to start imageMagick process";

        MvcResult mvcResult = mockMvc.perform(get("/text-extraction/emitter/{process-id}", processId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        Exception exception = assertThrows(
                ConvertingImageFileException.class,
                () -> validPipe.process(textExtractionDto));
        assertEquals(exception.getMessage(), expectedMessage);
        assertTrue(exception.getCause() instanceof IOException);

        mvcResult.getAsyncResult();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM))
                .andExpect(serverResponse -> {
                    sseResponseConverter.readValue(serverResponse.getResponse().getContentAsString());
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.FAILED),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.FAILED_CONVERTING),
                            () -> assertTrue(sseResponseConverter.getData().result().isEmpty())
                    );
                })
                .andDo(print());
    }

    @Test
    public void givenInvalidFile_whenProcess_thenThrowsConvertingImageFileException() throws Exception {
        textExtractionDto.setImageFileRecord(invalidFile);
        String expectedMessage = "ImageMagick internal error";

        MvcResult mvcResult = mockMvc.perform(get("/text-extraction/emitter/{process-id}", processId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        Exception exception = assertThrows(
                ConvertingImageFileException.class,
                () -> validPipe.process(textExtractionDto));
        assertTrue(exception.getMessage().contains(expectedMessage));

        mvcResult.getAsyncResult();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM))
                .andExpect((serverResponse -> {
                    sseResponseConverter.readValue(serverResponse.getResponse().getContentAsString());
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.FAILED),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.FAILED_CONVERTING),
                            () -> assertTrue(sseResponseConverter.getData().result().isEmpty())
                    );
                }))
                .andDo(print());
    }

    @Test
    public void givenSmallExecutionTime_whenProcess_thenThrowsConvertingImageFileException() throws Exception {
        textExtractionDto.setImageFileRecord(validEngPdf);
        String expectedMessage = "Timed out of execution";

        MvcResult mvcResult = mockMvc.perform(get("/text-extraction/emitter/{process-id}", processId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        Exception exception = assertThrows(
                ConvertingImageFileException.class,
                () -> pipeWithSmallExecutionTime.process(textExtractionDto));
        assertEquals(exception.getMessage(), expectedMessage);

        mvcResult.getAsyncResult();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM))
                .andExpect((serverResponse -> {
                    sseResponseConverter.readValue(serverResponse.getResponse().getContentAsString());
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.FAILED),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.FAILED_CONVERTING),
                            () -> assertTrue(sseResponseConverter.getData().result().isEmpty())
                    );
                }))
                .andDo(print());
    }

    @Test
    public void givenValidEngPdf_whenProcess_thenReturnValidPath() throws Exception {
        Path pathToConvertedFile = Path.of(uploads, validEngPdf.fileLocation(), "valid-eng-0.jpg");
        textExtractionDto.setImageFileRecord(validEngPdf);

        MvcResult mvcResult = mockMvc.perform(get("/text-extraction/emitter/{process-id}", processId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        TextExtractionDto result = validPipe.process(textExtractionDto);

        assertEquals(result.getConvertedFiles().get(0), pathToConvertedFile);
        assertTrue(Files.exists(pathToConvertedFile));

        SseEmitter sseEmitter = sseEmitters.get(processId);
        sseEmitter.complete();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM))
                .andExpect((serverResponse -> {
                    sseResponseConverter.readValue(serverResponse.getResponse().getContentAsString());
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.SUCCESSFUL),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.CONVERTED),
                            () -> assertTrue(sseResponseConverter.getData().result().isEmpty())
                    );
                }))
                .andDo(print());
    }
}
