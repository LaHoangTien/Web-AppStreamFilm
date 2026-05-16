package com.example.movie.controller;

import com.example.movie.model.Comment;
import com.example.movie.model.*;
import com.example.movie.model.Movie;
import com.example.movie.repository.CommentRepository;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.elasticsearch.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Date;

@Controller
@RequestMapping("/phim")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository; // Khai báo repository cho bình luận
    @Autowired
    private MovieRepository movieRepository; // Khai báo repository
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @RequestParam String commentText,
                             @RequestParam(required = false) Long parentCommentId,
                             Principal principal,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để bình luận.");
            return "redirect:/login"; // Chuyển hướng tới trang đăng nhập
        }

        // Lưu đường dẫn cũ để trả về
        String referer = request.getHeader("Referer");
        System.out.println("Referer: " + referer); // In ra referer để kiểm tra
        redirectAttributes.addFlashAttribute("previousUrl", referer);

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        Comment comment = new Comment();
        comment.setCommentText(commentText);
        comment.setCreatedAt(new Date());
        comment.setUser(user);
        comment.setUserName(username); // Lưu tên người dùng vào comment
        comment.setMovie(movie);

        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }

        commentRepository.save(comment);


        return "redirect:" + referer; // Chuyển hướng lại đường dẫn cũ
    }
    @DeleteMapping("/comments/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                Principal principal,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để xóa bình luận.");
            return "redirect:/login";
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Kiểm tra xem người dùng hiện tại có phải là người đăng bình luận hoặc là quản trị viên
        String username = principal.getName();
        if (!comment.getUser().getUsername().equals(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa bình luận này.");
            return "redirect:/phim/" + comment.getMovie().getMovieId();
        }

        commentRepository.delete(comment);

        // Chuyển hướng lại trang trước
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}

