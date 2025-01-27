package com.kcdevdes.poppick.controller;

import com.kcdevdes.poppick.domain.User;
import com.kcdevdes.poppick.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/v1/users/oauth/google")
public class OauthController {

    private final UserService userService;

    public OauthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<?> login() {
        // Spring Security의 설정에 따라 자동으로 Google 로그인 페이지로 리다이렉트됩니다.
        return ResponseEntity.ok("Redirecting to Google login...");
    }

    @GetMapping("/redirect")
    public ResponseEntity<?> handleRedirect(OAuth2AuthenticationToken authenticationToken) {
        if (authenticationToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }

        OAuth2User oAuth2User = authenticationToken.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String profileImage = oAuth2User.getAttribute("picture");

        // 사용자 등록 또는 처리 로직
        User user = userService.registerOauthUser(email, authenticationToken.getPrincipal().getName(),
                authenticationToken.getAuthorizedClientRegistrationId(), name, profileImage);

        if (user != null) {
            return ResponseEntity.ok("User authenticated: " + email);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User registration failed");
        }
    }


    @GetMapping("/success")
    public ResponseEntity<?> loginSuccess() {
        return ResponseEntity.ok("Login successful! You are now authenticated.");
    }

    @GetMapping("/fail")
    public ResponseEntity<?> loginFail() {
        return ResponseEntity.status(401).body("Login failed. Please try again.");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        return ResponseEntity.ok(oAuth2User.getAttributes());
    }
}
