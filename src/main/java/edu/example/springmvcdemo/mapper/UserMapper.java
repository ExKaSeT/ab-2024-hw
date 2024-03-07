package edu.example.springmvcdemo.mapper;

import edu.example.springmvcdemo.dto.user.UserResponseDto;
import edu.example.springmvcdemo.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);
}
