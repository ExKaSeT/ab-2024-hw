package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="processed_images")
public class ProcessedImage {
    @EmbeddedId
    @AttributeOverride(name="requestId", column=@Column(name="request_id"))
    @AttributeOverride(name="imageId", column=@Column(name="image_id"))
    private ProcessedImageId id;

    /**
     * После сохранения обработанного изображения нет гарантий, что событие об этом отправилось,
     * поэтому сохраняю и обработанное изображение
     * */
    @Column(name = "processed_image_id", nullable = false)
    private String processedImageId;
}
