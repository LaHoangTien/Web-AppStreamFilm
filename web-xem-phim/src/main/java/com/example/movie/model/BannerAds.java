package com.example.movie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class BannerAds {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Tên của banner
    private String position; // Vị trí (top, bottom, sidebar, ...)
    private String imageUrl; // URL hình ảnh banner
    private String redirectUrl; // URL chuyển hướng khi nhấp vào banner
    private Boolean isActive; // Trạng thái hoạt động của banner
    public Boolean isActive() {
        return isActive;
    }
    public void setActive(Boolean active) {
        this.isActive = active;
    }
}
