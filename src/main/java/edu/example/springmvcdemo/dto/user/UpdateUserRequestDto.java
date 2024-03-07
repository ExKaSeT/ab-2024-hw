package edu.example.springmvcdemo.dto.user;

import edu.example.springmvcdemo.validation.constraints.RoleConstraint;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserRequestDto {

    @NotEmpty
    private String username;

    @RoleConstraint
    private String role;

    private Boolean isBanned;
}
