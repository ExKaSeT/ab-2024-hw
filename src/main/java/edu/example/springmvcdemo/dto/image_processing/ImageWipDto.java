package edu.example.springmvcdemo.dto.image_processing;

import edu.example.springmvcdemo.model.ImageProcessingFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageWipDto {
    private String imageId;
    private String requestId;
    private List<ImageProcessingFilter> filters;
}
