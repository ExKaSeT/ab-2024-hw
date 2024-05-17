package edu.example.springmvcdemo.dto.image;

import edu.example.springmvcdemo.model.ImageProcessingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetModifiedImageByRequestIdResponseDto {
    @Schema(description = "ИД модифицированного или оригинального файла в случае отсутствия первого", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageId;
    @Schema(description = "Статус обработки файла", requiredMode = Schema.RequiredMode.REQUIRED)
    private ImageProcessingStatus status;
}
