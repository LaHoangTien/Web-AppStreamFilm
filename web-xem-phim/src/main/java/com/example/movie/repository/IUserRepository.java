package com.example.movie.repository;

import com.example.movie.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id); // Đảm bảo rằng phương thức này tồn tại
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchByUsernameOrEmailOrFullName(@Param("query") String query, Pageable pageable);


    // Đếm số lượng người dùng mới theo từng ngày trong tuần
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfWeek GROUP BY u.createdAt ORDER BY u.createdAt")
    List<Long> countNewUsersForWeek(LocalDate startOfWeek);

    long count();

    // Truy vấn số người dùng mới trong tuần
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfWeek")
    long countNewUsersThisWeek(LocalDate startOfWeek);
}