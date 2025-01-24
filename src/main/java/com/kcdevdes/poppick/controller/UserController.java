package com.kcdevdes.poppick.controller;

import com.kcdevdes.poppick.domain.User;
import com.kcdevdes.poppick.dto.*;
import com.kcdevdes.poppick.provider.JwtProvider;
import com.kcdevdes.poppick.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final JwtProvider jwtProvider;

    public UserController(UserService userService, ModelMapper modelMapper, JwtProvider jwtProvider) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/signup")
    public User signup(@Valid @RequestBody SignupRequestDto requestDto) {
        return userService.registerStandardUser(requestDto);
    }

    @PostMapping("/login")
    public JwtResponseDto login(@Valid @RequestBody LoginRequestDto requestDto) {
        return userService.login(requestDto);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyUser(@RequestHeader("Authorization") String token) {
        // Bearer 토큰에서 JWT 추출
        String jwt = extractEmailFromToken(token);

        if (!jwtProvider.validateToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // JWT에서 이메일 추출
        String email = jwtProvider.getEmailFromToken(jwt);

        // 이메일로 사용자 정보 검색
        User user = userService.getUserByEmail(email);

        // 필터링 (비밀번호, 권한 제거)
        UserResponseDto userResponseDto = UserResponseDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .profileImage(user.getProfileImage())
            .oauthProvider(user.getOauthProvider())
            .oauthId(user.getOauthId())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();

        return ResponseEntity.ok(userResponseDto);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyUser(@RequestHeader("Authorization") String token, @Valid @RequestBody UpdateUserRequestDto requestDto) {
        // Bearer 토큰에서 JWT 추출
        String jwt = extractEmailFromToken(token);

        if (!jwtProvider.validateToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // JWT에서 이메일 추출
        String email = jwtProvider.getEmailFromToken(jwt);

        // 이메일로 사용자 정보 검색
        User user = userService.getUserByEmail(email);
        if (!requestDto.getUsername().isEmpty()) {
            user.setUsername(requestDto.getUsername());
        }
        if (!requestDto.getProfileImage().isEmpty()) {
            user.setProfileImage(requestDto.getProfileImage());
        }

        return ResponseEntity.ok(userService.updateUser(user.getId(), user));
    }
//
//    @DeleteMapping("/me")
//    public void deleteMyUser(@RequestHeader("Authorization") String token) {
//        // Extract user email from token
//        String email;
//        userService.deleteUser(email);
//    }


    private String extractEmailFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtProvider.getEmailFromToken(jwt);
    }
}
