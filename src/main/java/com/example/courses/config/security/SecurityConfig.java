package com.example.courses.config.security;

import com.example.courses.config.security.exception.AccessDeniedHandler;
import com.example.courses.config.security.exception.AuthenticationEntryPoint;
import com.example.courses.config.security.jwt.JWTAuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JWTAuthTokenFilter jwtAuthTokenFilter;

    /**
     * Cấu hình mã hóa mật khẩu bằng BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình Authentication Provider sử dụng DAO
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    /**
     * Cấu hình Authentication Manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
        return auth.getAuthenticationManager();
    }

    /**
     * Cấu hình Security Filter Chain với logic phân quyền chi tiết
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Tắt CORS và CSRF (thường dùng cho REST API)
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình phân quyền truy cập
                .authorizeHttpRequests(request -> request
                        // ===== PUBLIC ENDPOINTS - Không cần xác thực =====
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/public/**").permitAll()

                        // ===== AUTHENTICATED ENDPOINTS - Cần xác thực JWT =====
                        .requestMatchers("/api/auth/verify").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/hello").authenticated()

                        // ===== LOGOUT ENDPOINTS - Cần xác thực =====
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout/all").authenticated()

                        // ===== ADMIN ENDPOINTS - Chỉ ADMIN =====
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ===== USER MANAGEMENT ENDPOINTS =====
                        // Lấy danh sách users, tạo user mới - chỉ ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")

                        // Lấy thông tin user - ADMIN hoặc chính user đó (sẽ check trong controller)
                        .requestMatchers(HttpMethod.GET, "/api/users/*").authenticated()

                        // Cập nhật thông tin cá nhân - OWNER_OR_ADMIN (sẽ check trong service)
                        .requestMatchers(HttpMethod.PUT, "/api/users/*").authenticated()

                        // Đổi mật khẩu - OWNER_OR_ADMIN (sẽ check trong service)
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/password").authenticated()

                        // Cập nhật role/status và xóa user - chỉ ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")

                        // ===== TEACHER ENDPOINTS - TEACHER và ADMIN =====
                        .requestMatchers("/api/teacher/**").hasAnyRole("TEACHER", "ADMIN")

                        // ===== STUDENT ENDPOINTS - STUDENT và ADMIN =====
                        .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN")

                        // ===== COURSE ENDPOINTS - Phân quyền theo HTTP method =====
                        .requestMatchers(HttpMethod.GET, "/api/courses").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")

                        // ===== LESSON ENDPOINTS - TEACHER và ADMIN =====
                        .requestMatchers(HttpMethod.POST, "/api/courses/*/lessons").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/lessons/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/lessons/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/lessons/**").authenticated()

                        // ===== ENROLLMENT ENDPOINTS - STUDENT và ADMIN =====
                        // GET /api/enrollments - Lấy danh sách đăng ký của mình
                        .requestMatchers(HttpMethod.GET, "/api/enrollments").hasAnyRole("STUDENT", "ADMIN")

                        // POST /api/enrollments - Đăng ký khóa học mới
                        .requestMatchers(HttpMethod.POST, "/api/enrollments").hasAnyRole("STUDENT", "ADMIN")

                        // GET /api/enrollments/{id} - Lấy chi tiết đăng ký
                        .requestMatchers(HttpMethod.GET, "/api/enrollments/*").hasAnyRole("STUDENT", "ADMIN")

                        // PUT /api/enrollments/{id}/complete_lesson/{lesson_id} - Hoàn thành bài học
                        .requestMatchers(HttpMethod.PUT, "/api/enrollments/*/complete_lesson/*").hasAnyRole("STUDENT", "ADMIN")

                        // ===== REVIEW ENDPOINTS - Cần xác thực =====
                        .requestMatchers("/api/reviews/**").authenticated()

                        // ===== NOTIFICATION ENDPOINTS - Cần xác thực =====
                        .requestMatchers("/api/notifications/**").authenticated()

                        // ===== Tất cả endpoints khác cần xác thực =====
                        .anyRequest().authenticated()
                )

                // Cấu hình session STATELESS (cho REST API)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Thêm JWT Filter trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class)

                // Đăng ký Authentication Provider
                .authenticationProvider(authenticationProvider())

                // Xử lý exception
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new AuthenticationEntryPoint())
                        .accessDeniedHandler(new AccessDeniedHandler())
                )
                .build();
    }
}