package edu.example.springmvcdemo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetImagesResponseDto {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ImageResponseDto> images;
}
