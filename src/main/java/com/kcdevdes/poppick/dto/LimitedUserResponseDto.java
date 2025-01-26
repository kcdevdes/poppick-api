package com.kcdevdes.poppick.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class LimitedUserResponseDto {
    private Integer id;
    private String email;
    private String username;
    private String profileImage;
}