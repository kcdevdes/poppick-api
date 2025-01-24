package com.kcdevdes.poppick.controller;

import com.kcdevdes.poppick.domain.User;
import com.kcdevdes.poppick.dto.LoginRequestDto;
import com.kcdevdes.poppick.dto.SignupRequestDto;
import com.kcdevdes.poppick.dto.UpdateUserRequestDto;
import com.kcdevdes.poppick.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public User signup(@Valid @RequestBody SignupRequestDto requestDto) {
        return userService.registerStandardUser(requestDto);
    }

    @PostMapping("/login")
    public User login(@Valid @RequestBody LoginRequestDto requestDto) {
        return userService.login(requestDto);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String token) {
        //jwt logout
    }

//    @GetMapping("/me")
//    public User getMyUser(@RequestHeader("Authorization") String token) {
//        // Extract user email from token
//        String email;
//        return userService.getUserByEmail(email);
//    }
//
//    @PutMapping("/me")
//    public User updateMyUser(@RequestHeader("Authorization") String token, @Valid @RequestBody UpdateUserRequestDto requestDto) {
//        // Extract user email from token
//        String email;
//
//        // Extract id from email
//        Optional<User> optionalUser = userService.getUserByEmail(email);
//        User user = optionalUser.orElseThrow(() -> new RuntimeException("User not found"));
//
//        return userService.updateUser(user.getId(), requestDto);
//    }
//
//    @DeleteMapping("/me")
//    public void deleteMyUser(@RequestHeader("Authorization") String token) {
//        // Extract user email from token
//        String email;
//        userService.deleteUser(email);
//    }
}
