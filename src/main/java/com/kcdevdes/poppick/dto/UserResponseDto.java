package com.kcdevdes.poppick.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserResponseDto {
    private Integer id;
    private String email;
    private String username;
    private String profileImage;
    private String oauthProvider;
    private String oauthId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

