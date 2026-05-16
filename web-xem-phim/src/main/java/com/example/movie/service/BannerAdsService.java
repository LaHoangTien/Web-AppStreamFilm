package com.example.movie.service;


import com.example.movie.model.BannerAds;
import com.example.movie.repository.BannerAdsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class BannerAdsService {

    private final BannerAdsRepository bannerAdsRepository;

    public BannerAdsService(BannerAdsRepository bannerAdsRepository) {
        this.bannerAdsRepository = bannerAdsRepository;
    }

    public List<BannerAds> getAllBanners() {
        return bannerAdsRepository.findAll();
    }

    public BannerAds getBannerById(Long id) {
        return bannerAdsRepository.findById(id).orElseThrow(() -> new RuntimeException("Banner not found"));
    }

    public void saveBanner(BannerAds banner) {
        bannerAdsRepository.save(banner);
    }

    public void deleteBanner(Long id) {
        bannerAdsRepository.deleteById(id);
    }
    public List<BannerAds> getActiveBannersByPosition(String position) {
        return bannerAdsRepository.findByPositionAndIsActive(position, true);
    }


        public String storeFile(MultipartFile file, String uploadDir) throws IOException {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + uploadDir + fileName; // Trả về URL để lưu vào cơ sở dữ liệu
        }

}
