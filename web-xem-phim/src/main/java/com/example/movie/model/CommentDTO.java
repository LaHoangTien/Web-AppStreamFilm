package com.example.movie.model;


import lombok.*;

import java.util.Date;
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    private Long commentId;
    private String commentText;
    private String userName; // Hoặc bất kỳ thông tin nào khác bạn muốn
    private Date createdAt;
    private Long parentCommentId; // ID của bình luận cha (nếu có)
    // Constructor, getters, and setters
}
