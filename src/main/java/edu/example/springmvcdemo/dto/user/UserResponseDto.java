package edu.example.springmvcdemo.dto.user;

import edu.example.springmvcdemo.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDto {
    private String username;
    private Role role;
    private boolean isBanned;
}
