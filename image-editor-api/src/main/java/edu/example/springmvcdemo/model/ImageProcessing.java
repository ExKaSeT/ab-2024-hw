package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "image_processing")
@Data
public class ImageProcessing {
    @Id
    @Column(length = 50, name = "request_id")
    private String requestId;

    @ManyToOne
    @JoinColumn(name = "original_image", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Image originalImage;

    @Column(length = 50, name = "processed_image")
    private String processedImage;

    @Column(nullable = false, length = 30, name = "status")
    @Enumerated(EnumType.STRING)
    private ImageProcessingStatus status;
}
