package com.kcdevdes.poppick.controller;

import com.kcdevdes.poppick.domain.User;
import com.kcdevdes.poppick.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

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
    public ResponseEntity<?> handleRedirect() {
        return ResponseEntity.ok("Redirected from Google. Processing...");
    }


    @GetMapping("/success")
public ResponseEntity<?> loginSuccess(OAuth2AuthenticationToken authenticationToken) {
    OAuth2User oAuth2User = authenticationToken.getPrincipal();
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");
    String profileImage = oAuth2User.getAttribute("picture");
    String oauthProvider = authenticationToken.getAuthorizedClientRegistrationId();
    String oauthId = oAuth2User.getName();

    Optional<User> userOptional = userService.getUserByEmail(email);

    if (userOptional.isPresent()) {
        // 기존 사용자 업데이트
        User user = userOptional.get();
        user.setUsername(name);
        user.setProfileImage(profileImage);
        user.setOauthProvider(oauthProvider);
        user.setOauthId(oauthId);
        userService.updateUser(user.getId(), user);
    } else {
        // 새로운 사용자 등록
        userService.registerOauthUser(email, oauthId, oauthProvider, name, profileImage);
    }

    return ResponseEntity.ok().body(userService.oauthLogin(email, oauthProvider, oauthId));
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
