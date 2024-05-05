package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.ImageProcessing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageProcessingRepository extends JpaRepository<ImageProcessing, String> {
}
