package edu.example.springmvcdemo.dto.imagga_image_tagger;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class ImaggaImageUploadResponseDto {
    private Result result;
    private ImaggaResponseStatus status;

    @Data
    public static class Result {
        @JsonAlias("upload_id")
        private String uploadId;
    }
}