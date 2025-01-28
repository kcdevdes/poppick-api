package com.kcdevdes.poppick.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LimitedUserResponseDto {
    private Integer id;
    private String email;
    private String username;
    private String profileImage;
}