package com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.pipe;

import com.smilingcatservice.eventdriventextextractor.textextracting.event.*;
import com.smilingcatservice.eventdriventextextractor.textextracting.exception.ConvertingImageFileException;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.ImageFileRecord;
import com.smilingcatservice.eventdriventextextractor.textextracting.payload.TextExtractionDto;
import com.smilingcatservice.eventdriventextextractor.textextracting.pipeline.Pipe;
import com.smilingcatservice.eventdriventextextractor.textextracting.service.TextExtractionEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ConvertingImageFilePipe implements Pipe {
    private final String commandMagick;
    private final int executionTimeLimit;
    private final String uploads;
    private final String CONVERTED_FILE_EXTENSION = "jpg";
    private final TextExtractionEventService eventPublisher;

    public ConvertingImageFilePipe(
            @Value("${app.image-magick.command}") String commandMagick,
            @Value("${app.image-magick.execution-time-limit}") int executionTimeLimit,
            @Value("${app.directory.uploads}") String uploads,
            TextExtractionEventService eventPublisher
    ) {
        this.commandMagick = commandMagick;
        this.uploads = uploads;
        this.executionTimeLimit = executionTimeLimit;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public TextExtractionDto process(TextExtractionDto input) {
        ImageFileRecord imageFileRecord = input.getImageFileRecord();
        List<Path> convertedFiles = getConvertedFiles(imageFileRecord);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(Path.of(uploads, imageFileRecord.fileLocation()).toFile());
        processBuilder.command(buildCommand(imageFileRecord.originalFilename()));

        try {
            Process imageMagick = processBuilder.start();

            CompletableFuture<String> imageMagickErrorMessage = CompletableFuture.supplyAsync(() -> {
                Scanner errorReader = new Scanner(imageMagick.getInputStream());
                StringBuilder message = new StringBuilder();

                while (errorReader.hasNextLine()) {
                    message.append(errorReader.nextLine());
                    message.append(System.lineSeparator());
                }

                return message.toString();
            });

            if (imageMagick.waitFor(executionTimeLimit, TimeUnit.SECONDS)) {
                if (imageMagick.exitValue() != 0) {
                    String errorMessage = "";

                    try {
                        errorMessage = imageMagickErrorMessage.get();
                    } catch (ExecutionException e) {
                        log.error("Failed to get error message from imageMagick", e);
                    }

                    eventPublisher.publishEvent(new FailedConvertingEvent(input.getProcessId()));
                    throw new ConvertingImageFileException("ImageMagick internal error" + System.lineSeparator() + errorMessage);
                }
            } else {
                imageMagick.destroyForcibly();
                eventPublisher.publishEvent(new FailedConvertingEvent(input.getProcessId()));
                throw new ConvertingImageFileException("Timed out of execution");
            }
        } catch (IOException e) {
            eventPublisher.publishEvent(new FailedConvertingEvent(input.getProcessId()));
            throw new ConvertingImageFileException("Failed to start imageMagick process", e);
        } catch (InterruptedException e) {
            eventPublisher.publishEvent(new FailedConvertingEvent(input.getProcessId()));
            throw new ConvertingImageFileException("ImageMagick process was interrupted", e);
        }

        eventPublisher.publishEvent(new CompletedConvertingEvent(input.getProcessId()));

        input.setConvertedFiles(convertedFiles);

        return input;
    }

    private List<String> buildCommand(String originalFilename) {
        List<String> command = new ArrayList<>(List.of(commandMagick.split(" ")));
        command.addAll(List.of(
                "-density", "300",
                "-quality", "100",
                "-sharpen", "0x1.0",
                "-alpha", "remove"
        ));
        command.add(originalFilename);
        command.add(getConvertedImageFilename(originalFilename));

        return command;
    }

    private String getConvertedImageFilename(String originalFilename) {

        return originalFilename.split("\\.")[0] + "-%d." + CONVERTED_FILE_EXTENSION;
    }

    private List<Path> getConvertedFiles(ImageFileRecord input) {
        List<Path> convertedFilenames = new ArrayList<>();

        for (int i=0; i<input.pageAmount(); i++) {
            String convertedFilename = input.originalFilename().split("\\.")[0] + "-" + i + "." + CONVERTED_FILE_EXTENSION;
            convertedFilenames.add(Path.of(uploads, input.fileLocation(), convertedFilename));
        }

        return convertedFilenames;
    }
}
