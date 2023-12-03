package com.smilingcatservice.eventdriventextextractor.extracting.controller;

import com.smilingcatservice.eventdriventextextractor.Config.SseResponseConfig;
import com.smilingcatservice.eventdriventextextractor.Config.SseResponseConverter;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.EventStatus;
import com.smilingcatservice.eventdriventextextractor.textextracting.event.MessageChannel;
import com.smilingcatservice.eventdriventextextractor.textextracting.repository.ImageFileRepository;
import com.smilingcatservice.eventdriventextextractor.uploading.entity.ImageFile;
import com.smilingcatservice.eventdriventextextractor.uploading.strategy.ImageFileType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = {SseResponseConfig.class})
@AutoConfigureMockMvc
public class TextExtractionControllerTests {
    private long imageFileId;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ImageFileRepository imageFileRepository;
    @Autowired
    private SseResponseConverter sseResponseConverter;

    @BeforeAll
    public void setup() {
        ImageFile imageFile = new ImageFile();
        imageFile.setType(ImageFileType.PDF);
        imageFile.setFileLocation("real-behavior");
        imageFile.setOriginalFilename("valid-eng.pdf");
        imageFile.setPageAmount(1);

        imageFileId = imageFileRepository.save(imageFile).getId();
    }

    @Test
    public void realBehavior() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/text-extraction/image-file/{image-file-id}/lang/{lang}", imageFileId, "eng"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isString())
                .andReturn();

        String processId = mvcResult.getResponse().getContentAsString();

        mvcResult = mockMvc.perform(get("/text-extraction/emitter/{process-id}", processId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        mvcResult.getAsyncResult(120000);

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(serverResponse -> {
                    String result = serverResponse.getResponse().getContentAsString();
                    String[] splitResult = result.split("\n\n");
                    String converted = splitResult[0];
                    String extracted = splitResult[1];
                    String formatted = splitResult[2];
                    sseResponseConverter.readValue(converted);
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.SUCCESSFUL),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.CONVERTED),
                            () -> assertTrue(sseResponseConverter.getData().result().isEmpty())
                    );
                    sseResponseConverter.readValue(extracted);
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.SUCCESSFUL),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.EXTRACTED),
                            () -> assertTrue(sseResponseConverter.getData().result().isEmpty())
                    );
                    sseResponseConverter.readValue(formatted);
                    assertAll(
                            () -> assertEquals(sseResponseConverter.getEvent(), MessageChannel.SUCCESSFUL),
                            () -> assertEquals(sseResponseConverter.getData().status(), EventStatus.FORMATTED),
                            () -> assertFalse(sseResponseConverter.getData().result().isEmpty()),
                            () -> assertTrue(sseResponseConverter.getData().result().contains("Summary: This role will be supporting"))
                    );
                });
    }
}
