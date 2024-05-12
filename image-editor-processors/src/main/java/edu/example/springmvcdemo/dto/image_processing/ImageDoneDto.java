package edu.example.springmvcdemo.dto.image_processing;

import edu.example.springmvcdemo.model.ImageProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDoneDto {
    private String imageId;
    private String requestId;
    private Integer sizeBytes;
    private ImageProcessingStatus status;
}
