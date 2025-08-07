package com.example.courses.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý danh sách token đã bị vô hiệu hóa (blacklist)
 */
@Service
@Slf4j
public class TokenBlacklistService {

    // Sử dụng ConcurrentHashMap để thread-safe
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Thêm token vào blacklist
     * @param token JWT token cần blacklist
     */
    public void blacklistToken(String token) {
        // Loại bỏ "Bearer " prefix nếu có
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && !token.isEmpty()) {
            blacklistedTokens.add(token);
            log.info("Token đã được thêm vào blacklist: {}",
                    token.substring(0, Math.min(token.length(), 20)) + "...");
        }
    }

    /**
     * Kiểm tra token có trong blacklist không
     * @param token JWT token cần kiểm tra
     * @return true nếu token đã bị blacklist
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Loại bỏ "Bearer " prefix nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return blacklistedTokens.contains(token);
    }



}