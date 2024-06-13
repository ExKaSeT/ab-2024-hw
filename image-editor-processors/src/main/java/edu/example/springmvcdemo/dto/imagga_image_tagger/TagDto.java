package edu.example.springmvcdemo.dto.imagga_image_tagger;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagDto {
    private String name;
    private double probability;
}
