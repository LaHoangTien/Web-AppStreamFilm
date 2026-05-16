package com.example.movie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID người dùng

    @Column(name = "username", length = 50, unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    private String username; // Tên đăng nhập

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    private String password; // Mật khẩu

    @Transient
    private String confirmPassword; // Xác nhận mật khẩu, không lưu vào DB

    @Column(name = "email", length = 50, unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email; // Email

    @Column(name = "full_name", length = 100)
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName; // Họ và tên
}
