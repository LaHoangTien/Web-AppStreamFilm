package com.example.movie.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId; // ID phim

    @Column(name = "title", nullable = false)
    private String title; // Tiêu đề phim

    @Column(name = "name", nullable = false)
    private String name; // Tiêu đề phim

    @Column(name = "slug")
    private String slug; // Đường dẫn đến video phim

    @Column(name = "description", length = 3000)
    private String description; // Mô tả phim

    @Column(name = "release_year")
    private Integer releaseYear; // Năm phát hành

    @Column(name = "director")
    private String director; // Đạo diễn phim

    @Column(name = "is_series")
    private Boolean isSeries; // Cờ xác định phim lẻ hay phim nhiều tập

    @Column(name = "duration")
    private Integer duration; // Thời gian dài của phim (tính bằng phút)

    @Column(name = "posterUrl")
    private String posterUrl; // Đường dẫn đến hình ảnh poster

    @Column(name = "backgroundUrl")
    private String backgroundUrl; // Đường dẫn đến hình ảnh poster

    @Column(name = "trailerUrl", length = 500)
    private String trailerUrl; // Đường dẫn đến video trailer

    @Column(name = "created_at")
    private java.util.Date createdAt; // Ngày thêm phim

    @Column(name = "updated_at")
    private java.util.Date updatedAt; // Ngày cập nhật phim

    @Column(name = "view_count")
    private Long viewCount = 0L; // Trường lưu trữ số lượt xem, khởi tạo mặc định là 0

    @Column(name = "weekly_view_count")
    private Long weeklyViewCount = 0L; // Số lượt xem trong tuần

    public Long getCalculatedViewCount() {
        long effectiveWeeklyViewCount = (weeklyViewCount != null) ? weeklyViewCount : 0L;
        return effectiveWeeklyViewCount;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Country country;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @JsonManagedReference
    private List<Genre> genres;




    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "movie_actor",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @JsonManagedReference
    private List<Actor> actors; // Danh sách diễn viên trong phim

    public boolean isSeries() {
        return isSeries; // Trả về giá trị của trường isSeries
    }

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Episode> episodes = new ArrayList<>();

    @OneToMany(mappedBy = "movie")
    @JsonManagedReference
    private List<Rating> ratings; // Danh sách đánh giá phim

    @OneToMany(mappedBy = "movie")
    @JsonManagedReference
    private List<Comment> comments; // Danh sách bình luận phim

    @Column(name = "total_episodes")
    private String totalEpisodes; // Tổng số tập của phim

    @Column(name = "movie_status")
    private String status;


}
