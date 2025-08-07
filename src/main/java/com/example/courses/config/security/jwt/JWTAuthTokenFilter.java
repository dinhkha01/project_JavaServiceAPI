package com.example.courses.config.security.jwt;

import com.example.courses.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Filter với kiểm tra blacklist
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JWTAuthTokenFilter extends OncePerRequestFilter {

    private final JWTProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = getTokenFromRequest(request);

            if (token != null) {
                // Kiểm tra token có trong blacklist không
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    log.warn("Attempted to use blacklisted token: {}...",
                            token.substring(0, Math.min(token.length(), 20)));
                    handleBlacklistedToken(response);
                    return;
                }

                // Kiểm tra token hợp lệ
                if (jwtProvider.validateToken(token)) {
                    String username = jwtProvider.getUserNameFromToken(token);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (userDetails != null) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.debug("User {} authenticated successfully", username);
                        }
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT authentication error: ", e);
            handleAuthenticationError(response, e.getMessage());
        }
    }

    /**
     * Lấy JWT token từ request header
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    /**
     * Xử lý khi token bị blacklist
     */
    private void handleBlacklistedToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", 401);
        errorResponse.put("message", "Token đã bị vô hiệu hóa");
        errorResponse.put("details", Map.of("error", "Token đã được đăng xuất"));

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * Xử lý lỗi authentication
     */
    private void handleAuthenticationError(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", 401);
        errorResponse.put("message", "Xác thực thất bại");
        errorResponse.put("details", Map.of("error", errorMessage));

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Không filter các endpoint public
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/public/");
    }
}