package edu.example.springmvcdemo.dto.imagga_image_tagger;

import lombok.Data;

@Data
public class ImaggaGetTagsResponseDto {
    private Result result;
    private ImaggaResponseStatus status;

    @Data
    public static class Result {
        private Tag[] tags;
    }

    @Data
    public static class Tag {
        private double confidence;
        private TagInfo tag;
    }

    @Data
    public static class TagInfo {
        private String en;
    }
}