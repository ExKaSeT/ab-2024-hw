package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dao.ImageProcessingRepository;
import edu.example.springmvcdemo.dto.SuccessContainerDto;
import edu.example.springmvcdemo.dto.image.ApplyImageFiltersResponseDto;
import edu.example.springmvcdemo.dto.image.GetModifiedImageByRequestIdResponseDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.ImageProcessingFilter;
import edu.example.springmvcdemo.service.ImageProcessingService;
import edu.example.springmvcdemo.validation.constraints.ImageFilterConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/image")
@Validated
@Tag(name = "Image Filters Controller", description = "Базовый CRUD API для работы с пользовательскими запросами на редактирование картинок")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class ImageFiltersController {

    private final ImageProcessingService imageProcessingService;
    private final ImageProcessingRepository imageProcessingRepository;

    @PostMapping("/{image-id}/filters/apply")
    @PreAuthorize("@imageSecurity.isAllowedToModifyImage(authentication, #imageId)")
    @Operation(summary = "Применение указанных фильтров к изображению")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = ApplyImageFiltersResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))})
    })
    public ApplyImageFiltersResponseDto applyImageFilters(@PathVariable("image-id") String imageId,
                                                          @Valid @RequestParam List<@ImageFilterConstraint String> filters) {
        var requestId = imageProcessingService.createApplyFiltersRequest(imageId,
                filters.stream().map(ImageProcessingFilter::valueOf).toList());
        return new ApplyImageFiltersResponseDto(requestId);
    }

    @GetMapping("/{image-id}/filters/{request-id}")
    @PreAuthorize("@imageSecurity.isAllowedToModifyImage(authentication, #imageId)")
    @Operation(summary = "Получение ИД измененного файла по ИД запроса")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = GetModifiedImageByRequestIdResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))})
    })
    public GetModifiedImageByRequestIdResponseDto getModifiedImageByRequestId(
            @PathVariable("image-id") String imageId,
            @PathVariable("request-id") String requestId) {
        var imageProcessing = imageProcessingRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("Request not found"));
        if (!imageProcessing.getOriginalImage().getLink().equals(imageId)) {
            throw new AccessDeniedException("Specified image does not belong to this request");
        }
        return imageProcessingService.getImageProcessingStatus(requestId);
    }
}