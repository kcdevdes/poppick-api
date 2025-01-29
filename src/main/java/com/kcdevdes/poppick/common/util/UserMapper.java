package com.kcdevdes.poppick.common.util;

import com.kcdevdes.poppick.entity.User;
import com.kcdevdes.poppick.dto.response.UserResponseDto;

public class UserMapper implements Mapper<User, UserResponseDto> {
    @Override
    public UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .profileImage(user.getProfileImage())
            .oauthProvider(user.getOauthProvider())
            .oauthId(user.getOauthId())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    @Override
    public User toEntity(UserResponseDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setProfileImage(dto.getProfileImage());
        user.setOauthProvider(dto.getOauthProvider());
        user.setOauthId(dto.getOauthId());
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());

        return user;
    }
}
