package com.example.movie.controller;

import com.example.movie.model.BannerAds;
import com.example.movie.model.User;
import com.example.movie.repository.UserRepository;
import com.example.movie.service.BannerAdsService;
import com.example.movie.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BannerAdsService bannerAdsService;

    @GetMapping("/login")
    public String login(Model model) {
        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);
        return "users/login";
    }

    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new User());
        List<BannerAds> topBanners = bannerAdsService.getActiveBannersByPosition("top");
        List<BannerAds> bottomBanners = bannerAdsService.getActiveBannersByPosition("bottom");
        model.addAttribute("topBanners", topBanners);
        model.addAttribute("bottomBanners", bottomBanners);
        return "users/register"; // Trả về đúng đường dẫn template
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           @NotNull BindingResult bindingResult,
                           Model model) {
        // Kiểm tra nếu mật khẩu và xác nhận mật khẩu không khớp
        if (!user.isPasswordConfirmed()) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }

        // Kiểm tra nếu có lỗi validate
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            return "users/register"; // Trả về lại view "register" nếu có lỗi
        }

        userService.save(user);
        userService.setDefaultRole(user.getUsername());
        return "redirect:/login";
    }

    @PostMapping("user/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email không tồn tại trong hệ thống.");
            return "redirect:/login"; // Chuyển hướng về login với thông báo lỗi
        }

        User user = userOptional.get();

        // Tạo mật khẩu mới và lưu vào database
        String newPassword = generateRandomPassword(10);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Gửi email chứa mật khẩu mới
        try {
            sendEmail(email, newPassword);
            redirectAttributes.addFlashAttribute("message", "Mật khẩu mới đã được gửi đến email của bạn.");
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi gửi email.");
            e.printStackTrace();
        }

        return "redirect:/login"; // Chuyển hướng về login
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }
    private void sendEmail(String recipientEmail, String newPassword) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(recipientEmail);
        helper.setSubject("Khôi phục mật khẩu");
        helper.setText("Mật khẩu mới của bạn là: " + newPassword);

        mailSender.send(message);
    }

}

