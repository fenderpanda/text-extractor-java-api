package com.smilingcatservice.eventdriventextextractor.uploading.entity;

import com.smilingcatservice.eventdriventextextractor.uploading.strategy.ImageFileType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "image_file")
public class ImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private ImageFileType type;
    private String originalFilename;
    private String fileLocation;
    private int pageAmount;

    @UpdateTimestamp
    private LocalDateTime updated;
    @CreationTimestamp
    private LocalDateTime created;
}
