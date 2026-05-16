package com.example.movie.controller;

import com.example.movie.model.Users;
import com.example.movie.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAPIController {

    private final UsersService userService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody Users user) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return "Username đã tồn tại.";
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            return "Mật khẩu không khớp.";
        }

        userService.save(user);
        return "Đăng ký thành công!";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        Optional<Users> user = userService.findByUsername(username);

        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return "Đăng nhập thành công!";
        }

        return "Tên đăng nhập hoặc mật khẩu không đúng.";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        Optional<Users> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            return "Email không tồn tại.";
        }

        // Đặt lại mật khẩu
        Users user = userOptional.get();
        String newPassword = generateRandomPassword(10);
        user.setPassword(newPassword);
        userService.save(user);

        // Có thể gửi email với mật khẩu mới ở đây (giả lập trong ví dụ này)
        return "Mật khẩu mới là: " + newPassword;
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}
