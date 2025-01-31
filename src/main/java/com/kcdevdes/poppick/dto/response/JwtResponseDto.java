package com.kcdevdes.poppick.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JwtResponseDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
