package com.kcdevdes.poppick.util;

import com.kcdevdes.poppick.domain.User;
import com.kcdevdes.poppick.dto.LimitedUserResponseDto;

public class LimitedUserMapper implements Mapper<User, LimitedUserResponseDto> {
    @Override
    public LimitedUserResponseDto toDto(User entity) {
        return LimitedUserResponseDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .profileImage(entity.getProfileImage())
                .build();
    }

    @Override
    public User toEntity(LimitedUserResponseDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setProfileImage(dto.getProfileImage());

        return user;
    }
}
