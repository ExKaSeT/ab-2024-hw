package edu.example.springmvcdemo.dto.image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponseDto {
    /**
     Original filename
     */
    private String filename;
    private Integer size;
    /**
     Object name in storage
     */
    private String imageId;
}
