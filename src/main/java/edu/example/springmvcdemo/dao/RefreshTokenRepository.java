package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteAllByUserUsername(String username);
}
