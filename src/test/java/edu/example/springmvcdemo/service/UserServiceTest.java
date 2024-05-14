package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.annotation.IntegrationTest;
import edu.example.springmvcdemo.dao.UserRepository;
import edu.example.springmvcdemo.model.Role;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
public class UserServiceTest {

    private final UserService userService;
    private final UserRepository userRepository;

    @BeforeEach
    @AfterEach
    public void clear() {
        userRepository.deleteAll();
    }

    @Test
    public void createAndGet() {
        String username = "username";
        String password = "password";
        Role role = Role.USER;

        userService.createUser(username, password, role);
        var user = userService.getUserByUsername(username);

        var users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
        assertEquals(username, user.getUsername());
        assertEquals(role, user.getRole());
    }

    @Test
    public void notExists() {
        assertFalse(userService.isUserExist("username"));
    }
}
