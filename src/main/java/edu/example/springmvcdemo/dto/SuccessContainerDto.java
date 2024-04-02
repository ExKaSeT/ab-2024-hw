package edu.example.springmvcdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessContainerDto {
    @Schema(description = "Признак успеха", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;

    @Schema(description = "Сообщение об ошибке")
    private String message;
}
