package com.example.courses.config.security;

import com.example.courses.config.security.exception.AccessDeniedHandler;
import com.example.courses.config.security.exception.AuthenticationEntryPoint;
import com.example.courses.config.security.jwt.JWTAuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
     * Cấu hình Security Filter Chain với logic phân quyền cho hệ thống khóa học
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Tắt CORS và CSRF (thường dùng cho REST API)
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình phân quyền truy cập
                .authorizeHttpRequests(request -> request
                        // 1. PUBLIC ENDPOINTS - Không cần xác thực
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/public/**").permitAll()

                        // 2. AUTHENTICATED ENDPOINTS - Cần xác thực JWT
                        .requestMatchers("/api/auth/verify").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/hello").authenticated()

                        // 3. ADMIN ENDPOINTS - Chỉ ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 4. USER MANAGEMENT ENDPOINTS - Chỉ ADMIN
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 5. TEACHER ENDPOINTS - Chỉ TEACHER và ADMIN
                        .requestMatchers("/api/teacher/**").hasAnyRole("TEACHER", "ADMIN")

                        // 6. STUDENT ENDPOINTS - Chỉ STUDENT và ADMIN
                        .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN")

                        // 7. COURSE ENDPOINTS - Tùy thuộc vào phương thức HTTP
                        .requestMatchers("GET", "/api/courses/**").permitAll() // Xem khóa học public
                        .requestMatchers("POST", "/api/courses").hasAnyRole("TEACHER", "ADMIN") // Tạo khóa học
                        .requestMatchers("PUT", "/api/courses/**").hasAnyRole("TEACHER", "ADMIN") // Sửa khóa học
                        .requestMatchers("DELETE", "/api/courses/**").hasAnyRole("TEACHER", "ADMIN") // Xóa khóa học

                        // 8. ENROLLMENT ENDPOINTS - Cần xác thực
                        .requestMatchers("/api/enrollments/**").authenticated()

                        // 9. REVIEW ENDPOINTS - Cần xác thực
                        .requestMatchers("/api/reviews/**").authenticated()

                        // 10. NOTIFICATION ENDPOINTS - Cần xác thực
                        .requestMatchers("/api/notifications/**").authenticated()

                        // 11. Tất cả endpoints khác cần xác thực
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