package com.example.courses.config.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthTokenFilter extends OncePerRequestFilter {

    private final JWTProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lấy token từ request header
        String token = getTokenFromRequest(request);

        // Xác thực và giải mã token
        if (token != null && jwtProvider.validateToken(token)) {
            String username = jwtProvider.getUserNameFromToken(token);

            // Tải thông tin user và tạo authentication object
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Lưu authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Tiếp tục filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Trích xuất JWT token từ Authorization header
     * @param request HTTP request
     * @return JWT token hoặc null nếu không tìm thấy
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7); // Bỏ "Bearer " prefix
        }

        return null;
    }
}