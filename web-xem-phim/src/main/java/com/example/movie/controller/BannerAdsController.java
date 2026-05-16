package com.example.movie.controller;


import com.example.movie.model.BannerAds;
import com.example.movie.service.BannerAdsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/banners")
public class BannerAdsController {

    private final BannerAdsService bannerAdsService;

    public BannerAdsController(BannerAdsService bannerAdsService) {
        this.bannerAdsService = bannerAdsService;
    }

    @GetMapping
    public String listBanners(Model model) {
        model.addAttribute("banners", bannerAdsService.getAllBanners());
        return "banners/banner-list";
    }

    @GetMapping("/add")
    public String showAddBannerForm(Model model) {
        model.addAttribute("banner", new BannerAds());
        return "banners/banner-add";
    }

    @GetMapping("/edit/{id}")
    public String showEditBannerForm(@PathVariable Long id, Model model) {
        BannerAds banner = bannerAdsService.getBannerById(id);
        model.addAttribute("banner", banner);
        return "banners/banner-edit";
    }

    @PostMapping("/save")
    public String saveBanner(@ModelAttribute("banner") BannerAds banner,
                             @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                // Đường dẫn đến thư mục "static/assets/images/banners"
                String uploadDir = "src/main/resources/static/assets/images/banners/";

                // Đảm bảo thư mục tồn tại
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath); // Tạo thư mục nếu chưa tồn tại
                }

                // Lưu file vào thư mục
                String fileName = imageFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Lưu URL file (tương đối) vào banner
                banner.setImageUrl("/assets/images/banners/" + fileName);
            }
        bannerAdsService.saveBanner(banner);
        return "redirect:/banners";
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/admin/banners?error=true"; // Chuyển hướng nếu xảy ra lỗi
        }
    }
    @PostMapping("/toggle-active/{id}")
    public ResponseEntity<String> toggleBannerActive(@PathVariable Long id) {
        try {
            BannerAds banner = bannerAdsService.getBannerById(id);
            banner.setActive(!banner.isActive()); // Correct method
            bannerAdsService.saveBanner(banner); // Save the updated banner
            return ResponseEntity.ok("Trạng thái đã được cập nhật!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi!");
        }
    }
    @GetMapping("/delete/{id}")
    public String deleteBanner(@PathVariable Long id) {
        bannerAdsService.deleteBanner(id);
        return "redirect:/banners";
    }
}

