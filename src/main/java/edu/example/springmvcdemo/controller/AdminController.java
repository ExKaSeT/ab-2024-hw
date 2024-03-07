package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dto.PageResponse;
import edu.example.springmvcdemo.dto.user.UpdateUserRequestDto;
import edu.example.springmvcdemo.dto.user.UserRequestDto;
import edu.example.springmvcdemo.dto.user.UserResponseDto;
import edu.example.springmvcdemo.mapper.UserMapper;
import edu.example.springmvcdemo.model.Role;
import edu.example.springmvcdemo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static java.util.Objects.isNull;

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin panel functionality")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/users")
    @Operation(description = "Get users")
    public PageResponse<UserResponseDto> getUsers(@RequestBody UserRequestDto filter) {
        var result = userService.getUsers(filter.toPredicate(), filter.getPageNumber(), filter.getPageSize());

        var response = new PageResponse<UserResponseDto>();
        response.setPageSize(result.getSize());
        response.setPageNumber(result.getNumber());
        response.setTotalPages(result.getTotalPages());
        response.setTotalSize(result.getTotalElements());
        response.setContent(result.getContent().stream().map(userMapper::toUserResponseDto).toList());

        return response;
    }

    @PostMapping("/users")
    @Operation(description = "Update user")
    public UserResponseDto updateUser(@RequestBody @Valid UpdateUserRequestDto dto) {
        Role role = isNull(dto.getRole()) ? null : Role.valueOf(dto.getRole());
        return userMapper.toUserResponseDto(userService.updateUser(dto.getUsername(), dto.getIsBanned(), role));
    }
}
