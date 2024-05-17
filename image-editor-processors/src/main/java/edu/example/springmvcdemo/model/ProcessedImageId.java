package edu.example.springmvcdemo.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class ProcessedImageId implements Serializable {
    private String requestId;
    private String imageId;
}
