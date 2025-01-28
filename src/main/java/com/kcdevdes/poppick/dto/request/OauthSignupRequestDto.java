package com.kcdevdes.poppick.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OauthSignupRequestDto {
    public String email;
    public String oauthId;
    public String oauthProvider;
    public String username;
    public String profileImage;
}