package edu.example.springmvcdemo.service;

import com.querydsl.core.types.Predicate;
import edu.example.springmvcdemo.dao.UserRepository;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.Role;
import edu.example.springmvcdemo.model.User;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static java.util.Objects.nonNull;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public Page<User> getUsers(Predicate predicate, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(
                Sort.Order.asc("username")
        ));

        return userRepository.findAll(predicate, pageable);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new EntityNotFoundException("User with specified username not found"));
    }

    public User createUser(String username, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepository.save(user);
    }

    public boolean isUserExist(String username) {
        return userRepository.existsByUsername(username);
    }

    public User updateUser(String username, @Nullable Boolean isBanned, @Nullable Role role) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Updating admin is not allowed");
        }

        if (nonNull(isBanned)) {
            if (isBanned) {
                tokenService.deactivateUserTokens(user.getUsername());
            }
            user.setBanned(isBanned);
        }
        if (nonNull(role)) {
            user.setRole(role);
        }
        return userRepository.save(user);
    }
}