package com.kcdevdes.poppick.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {
    @Size(min = 4, message = "Name must be at least 4 characters long")
    private String username;

    @URL(message = "Profile image must be a valid URL")
    private String profileImage;
}
