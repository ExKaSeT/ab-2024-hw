package edu.example.springmvcdemo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyImageFiltersResponseDto {
    @Schema(description = "ИД запроса в системе", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestId;
}
