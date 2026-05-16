package com.example.movie.model;

import com.example.movie.repository.PasswordCheckGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users") // Đổi tên bảng thành 'users' cho nhất quán
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID người dùng

    @Column(name = "username", length = 50, unique = true)
    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    private String username; // Tên người dùng

    @Column(name = "password", length = 250)
    private String password; // Mật khẩu

    @Transient
    @NotBlank(message = "Confirm Password is required", groups = PasswordCheckGroup.class)
    private String confirmPassword;

    @Column(name = "email", length = 50, unique = true)
    @NotBlank(message = "Email is required")
    @Size(min = 1, max = 50, message = "Email must be between 1 and 50 characters")
    @Email
    private String email; // Địa chỉ email

    @Column(name = "full_name", length = 100)
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName; // Họ và tên

    @Column(name = "provider", length = 50)
    private String provider; // Nhà cung cấp



    @Column(name = "profile_image", length = 255)
    private String profileImage; // Đường dẫn hình ảnh profile

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd/MM/yyyy") // Định dạng ngày
    private LocalDate createdAt; // Ngày tạo tài khoản của người dùng

    @PrePersist
    public void setCreatedAt() {
        if (createdAt == null) {
            this.createdAt = LocalDate.now(); // Gán ngày hiện tại nếu createdAt chưa có giá trị
        }
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>(); // Các vai trò của người dùng

    @OneToMany(mappedBy = "user")
    private List<Favorites> favorites; // Danh sách phim yêu thích

    @OneToMany(mappedBy = "user")
    private List<Rating> reviews; // Danh sách đánh giá

    @OneToMany(mappedBy = "user")
    private List<WatchHistory> watchHistory; // Lịch sử xem phim

    @OneToMany(mappedBy = "user")
    private List<Comment> comments; // Danh sách bình luận

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> userRoles = this.getRoles();
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean isPasswordConfirmed() {
        return this.password != null && this.password.equals(this.confirmPassword);
    }

    public Long getId() {
        return id;
    }
}