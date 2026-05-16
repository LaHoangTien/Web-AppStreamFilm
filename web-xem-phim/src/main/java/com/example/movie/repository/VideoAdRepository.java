package com.example.movie.repository;

import com.example.movie.model.VideoAd;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VideoAdRepository extends JpaRepository<VideoAd, Long> {
    // Tìm quảng cáo video đang hoạt động
    VideoAd findTopByAdStatusOrderByAdIdDesc(String adStatus);
}