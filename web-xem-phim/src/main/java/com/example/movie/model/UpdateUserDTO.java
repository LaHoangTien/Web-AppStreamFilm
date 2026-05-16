package com.example.movie.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private MultipartFile profileImage;

    private String currentPassword;

    private String newPassword;

    private String confirmPassword;
}