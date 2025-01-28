package com.kcdevdes.poppick.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {
    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Username is required")
    @Size(min = 4, max = 30, message = "Name must be at least 4 characters long")
    private String username;

    @NotNull(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 6 characters long")
    private String password;
}