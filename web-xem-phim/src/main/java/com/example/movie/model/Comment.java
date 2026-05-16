package com.example.movie.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId; // ID của bình luận

    @ManyToOne(fetch = FetchType.LAZY) // Hoặc FetchType.EAGER tùy vào nhu cầu
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference
    private Movie movie; // Phim mà bình luận thuộc về

    @Column(name = "comment_text", nullable = false)
    private String commentText; // Nội dung bình luận

    @Column(name = "created_at", nullable = false)
    private Date createdAt; // Ngày tạo bình luận

    @Column(name = "user_name", nullable = false)
    private String userName;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id") // Tham chiếu đến bình luận cha
    private Comment parentComment; // Bình luận cha

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Comment> replies; // Danh sách bình luận con

}
