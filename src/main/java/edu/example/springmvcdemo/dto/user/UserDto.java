package edu.example.springmvcdemo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {

    private String username;
    private String email;
}
