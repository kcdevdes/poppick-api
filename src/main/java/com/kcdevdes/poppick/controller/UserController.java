package com.kcdevdes.poppick.controller;

import com.kcdevdes.poppick.domain.User;
import com.kcdevdes.poppick.dto.*;
import com.kcdevdes.poppick.util.JwtProvider;
import com.kcdevdes.poppick.util.LimitedUserMapper;
import com.kcdevdes.poppick.util.UserMapper;
import com.kcdevdes.poppick.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper = new UserMapper();
    private final LimitedUserMapper limitedUserMapper = new LimitedUserMapper();
    private final JwtProvider jwtProvider;

    public UserController(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
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
        // Extract user email from token
        String jwt = extractEmailFromToken(token);

        if (!jwtProvider.validateToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Retrieve user email from token
        String email = jwtProvider.getEmailFromToken(jwt);
        User user = userService.getUserByEmail(email);

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyUser(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UpdateUserRequestDto requestDto) {

        // JWT 검증 및 이메일 추출
        String jwt = extractEmailFromToken(token);
        if (!jwtProvider.validateToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String email = jwtProvider.getEmailFromToken(jwt);

        // 사용자 검색
        User user = userService.getUserByEmail(email);

        // 요청 DTO에 따라 사용자 정보 업데이트
        if (requestDto.getUsername() != null && !requestDto.getUsername().isEmpty()) {
            user.setUsername(requestDto.getUsername());
        }
        if (requestDto.getProfileImage() != null && !requestDto.getProfileImage().isEmpty()) {
            user.setProfileImage(requestDto.getProfileImage());
        }

        // 사용자 정보 저장
        User updatedUser = userService.updateUser(user.getId(), user);

        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }


    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyUser(@RequestHeader("Authorization") String token) {
        // Extract user email from token
        String jwt = extractEmailFromToken(token);

        if (!jwtProvider.validateToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        // Retrieve user email from token
        String email = jwtProvider.getEmailFromToken(jwt);
        User user = userService.getUserByEmail(email);

        userService.deleteUser(user.getId());
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<LimitedUserResponseDto> getUser(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(limitedUserMapper.toDto(user));
    }

    private String extractEmailFromToken(String token) {
        return token.replace("Bearer ", "");
    }
}
