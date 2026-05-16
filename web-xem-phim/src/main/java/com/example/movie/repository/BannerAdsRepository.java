package com.example.movie.repository;

import com.example.movie.model.BannerAds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerAdsRepository extends JpaRepository<BannerAds, Long> {
    List<BannerAds> findByPositionAndIsActive(String position, boolean isActive);
}