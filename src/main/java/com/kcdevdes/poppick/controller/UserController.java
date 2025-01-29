package com.kcdevdes.poppick.controller;

import com.kcdevdes.poppick.dto.request.UpdateUserRequestDto;
import com.kcdevdes.poppick.dto.response.LimitedUserResponseDto;
import com.kcdevdes.poppick.dto.response.UserResponseDto;
import com.kcdevdes.poppick.entity.User;
import com.kcdevdes.poppick.common.provider.JwtProvider;
import com.kcdevdes.poppick.common.util.LimitedUserMapper;
import com.kcdevdes.poppick.common.util.UserMapper;
import com.kcdevdes.poppick.service.UserService;
import jakarta.validation.Valid;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@RequestHeader("Authorization") String token) {
        // Extract email from token
        String jwt = extractTokenFromHeader(token);
        String email = jwtProvider.getEmailFromToken(jwt);

        // Search for user
        User user = userService.getUserByEmail(email);

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyUser(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UpdateUserRequestDto requestDto) {
        // Extract email from token
        String jwt = extractTokenFromHeader(token);
        String email = jwtProvider.getEmailFromToken(jwt);

        // Search for user
        User user = userService.getUserByEmail(email);

        // Update user
        if (requestDto.getProfileImage() != null) {
            user.setProfileImage(requestDto.getProfileImage());
        }
        if (requestDto.getUsername() != null) {
            user.setUsername(requestDto.getUsername());
        }
        return ResponseEntity.ok(userMapper.toDto(userService.updateUserByEmail(user.getEmail(), user)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<LimitedUserResponseDto> deleteMyUser(@RequestHeader("Authorization") String token) {
        // Extract user email from token
        String jwt = extractTokenFromHeader(token);
        String email = jwtProvider.getEmailFromToken(jwt);

        // Get User by email
        User user = userService.getUserByEmail(email);

        // Delete user
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok(limitedUserMapper.toDto(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LimitedUserResponseDto> getUser(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(limitedUserMapper.toDto(user));
    }

    /**
     * Extract token from header
     * @param token
     * @return token - without "Bearer "
     */
    private String extractTokenFromHeader(String token) {
        return token.replace("Bearer ", "");
    }
}
