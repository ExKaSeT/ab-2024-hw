package edu.example.springmvcdemo.dao;


import edu.example.springmvcdemo.model.Role;
import edu.example.springmvcdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);

    boolean existsByUsername(String username);
}
