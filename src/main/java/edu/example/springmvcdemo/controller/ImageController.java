package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dto.SuccessContainerDto;
import edu.example.springmvcdemo.dto.image.GetImagesResponseDto;
import edu.example.springmvcdemo.dto.image.UploadImageResponseDto;
import edu.example.springmvcdemo.mapper.ImageMapper;
import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.ImageService;
import edu.example.springmvcdemo.validation.constraints.ImageExtensionConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import static java.util.Objects.isNull;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Image Controller", description = "Базовый CRUD API для работы с картинками")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageMapper mapper;

    @PostMapping("/image")
    @Operation(summary = "Загрузка нового изображения в систему")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = UploadImageResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "Файл не прошел валидацию",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))})
    })
    public UploadImageResponseDto uploadImage(@RequestPart(name = "file") @Valid @ImageExtensionConstraint MultipartFile file,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return mapper.toUploadImageResponseDto(imageService.upload(file, userDetails.getUser()));
    }

    @GetMapping("/image/{imageId}")
    @PreAuthorize("@imageSecurity.isAllowedToModifyImage(authentication, #imageId)")
    @Operation(summary = "Скачивание файла по ИД")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(mediaType = "*/*", schema = @Schema(implementation = MultipartFile.class))}),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))})
    })
    public ResponseEntity<Resource> downloadImage(@PathVariable String imageId) {
        var file = imageService.getMeta(imageId);

        InputStream fileInputStream = imageService.get(imageId);
        if (isNull(fileInputStream)) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(fileInputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/image/{imageId}")
    @PreAuthorize("@imageSecurity.isAllowedToModifyImage(authentication, #imageId)")
    @Operation(summary = "Удаление файла по ИД")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))}),
            @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))})
    })
    public ResponseEntity<?> deleteImage(@PathVariable("imageId") String imageId) {
        if (!imageService.exists(imageId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SuccessContainerDto(false, "Image not found or unavailable"));
        }

        imageService.delete(imageId);
        return ResponseEntity.ok(new SuccessContainerDto(true, null));
    }

    @GetMapping("/images")
    @Operation(summary = "Получение списка изображений, которые доступны пользователю")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
                    content = {@Content(schema = @Schema(implementation = GetImagesResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
                    content = {@Content(schema = @Schema(implementation = SuccessContainerDto.class))})
    })
    public GetImagesResponseDto getImages(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        var images = imageService.getUserImageMetas(userDetails.getUser())
                .stream().map(mapper::toImageResponseDto).toList();
        return new GetImagesResponseDto(images);
    }
}