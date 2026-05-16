package com.example.movie;

import com.example.movie.service.CustomOAuth2UserService;
import com.example.movie.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.filters.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration // Đánh dấu lớp này là một lớp cấu hình cho Spring Context.
@EnableWebSecurity // Kích hoạt tính năng bảo mật web của Spring Security.
@RequiredArgsConstructor // Lombok tự động tạo constructor có tham số cho tất cả các trường final.
public class SecurityConfig  {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserService userService; // Tiêm UserService vào lớp cấu hình này.
    @Bean // Đánh dấu phương thức trả về một bean được quản lý bởi Spring Context.
    public UserDetailsService userDetailsService() {
        return new UserService(); // Cung cấp dịch vụ xử lý chi tiết người dùng.
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Bean mã hóa mật khẩu sử dụng BCrypt.
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var auth = new DaoAuthenticationProvider(); // Tạo nhà cung cấp xác thực.
        auth.setUserDetailsService(userDetailsService()); // Thiết lập dịch vụ chi tiết người dùng.
        auth.setPasswordEncoder(passwordEncoder()); // Thiết lập cơ chế mã hóa mật khẩu.
        return auth; // Trả về nhà cung cấp xác thực.
    }


    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**","/livesearch","/live") // Bỏ qua CSRF cho endpoint cụ thể
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/API", "/css/**", "/js/**", "/oauth/**", "/register", "/login", "/error",
                                "/vendor/**", "/assets/**", "/phim/**","/oauth2/authorization/", "/comments/add", "/locphim/**",
                                "/series/**","/phimle/**","/phimbo/**","/forgot-password","user/forgot-password","/profile_images/**",
                                "/add-actors-page","/livesearch","/live","/search") // Đường dẫn cho phép truy cập không cần xác thực
                        .permitAll()
                        .requestMatchers("/favorites/toggle") // Đường dẫn để thêm/xóa yêu thích
                        .permitAll() // Cho phép truy cập không cần xác thực
                        .requestMatchers("/users/delete","/users/**","/movies/**","/admin", "/actors/**", "/genres/**", "/countries/**","/banners/**")
                        .hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/**")
                        .permitAll() // API mở cho mọi người dùng
                        .anyRequest().authenticated() // Bất kỳ yêu cầu nào khác cần xác thực

                ).

                logout(logout -> logout
                        .logoutUrl("/logout") // Đường dẫn cho logout
                        .logoutSuccessUrl("/") // Trang chuyển hướng sau khi logout thành công
                        .permitAll()
                ) .
                formLogin(formLogin -> formLogin
                        .loginPage("/login") // Trang đăng nhập.
                        .loginProcessingUrl("/login") // URL xử lý đăng nhập.
                        .defaultSuccessUrl("/admin") // Trang sau đăng nhập thành công.
                        .failureUrl("/login?error") // Trang đăng nhập thất bại.
                        .permitAll()
                ) .
                rememberMe(rememberMe -> rememberMe
                        .key("hutech")
                        .rememberMeCookieName("hutech")
                        .tokenValiditySeconds(24 * 60 * 60) // Thời gian nhớ đăng nhập.
                        .userDetailsService(userDetailsService())
                ) .
                exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/403") // Trang báo lỗi khi truy cập không được phép.
                ) .
                sessionManagement(sessionManagement -> sessionManagement
                        .maximumSessions(1) // Giới hạn số phiên đăng nhập.
                        .expiredUrl("/login") // Trang khi phiên hết hạn.
                ) .
                httpBasic(httpBasic -> httpBasic
                        .realmName("hutech") // Tên miền cho xác thực cơ bản.
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                ).build();
    }

}