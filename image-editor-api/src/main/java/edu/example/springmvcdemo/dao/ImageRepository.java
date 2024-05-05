package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.Image;
import edu.example.springmvcdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByLink(String name);

    List<Image> findAllByUser(User user);

    void deleteByLink(String link);

    boolean existsByLink(String link);
}
