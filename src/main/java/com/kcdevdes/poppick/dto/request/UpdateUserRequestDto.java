package com.kcdevdes.poppick.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {
    @Size(min = 4, max = 30, message = "Username must be between 4 and 30 characters")
    private String username;

    @URL(message = "Profile image must be a valid URL")
    private String profileImage;
}
