package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.ProcessedImage;
import edu.example.springmvcdemo.model.ProcessedImageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedImageRepository extends JpaRepository<ProcessedImage, ProcessedImageId> {
}
