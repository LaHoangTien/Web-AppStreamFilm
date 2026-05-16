package com.example.movie.model;
import jakarta.persistence.*;
import lombok.*;


@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "video_ads")
public class VideoAd {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adId; // ID quảng cáo

    @Column(name = "video_url", nullable = false)
    private String videoUrl; // URL của quảng cáo video

    @Column(name = "skip_time", nullable = false)
    private int skipTime; // Thời gian bỏ qua sau bao lâu (ví dụ: 5 giây)

    @Column(name = "duration", nullable = false)
    private int duration; // Tổng thời gian quảng cáo (ví dụ: 30 giây)

    @Column(name = "ad_status", nullable = false)
    private String adStatus; // Trạng thái của quảng cáo (active, paused, completed)
}
