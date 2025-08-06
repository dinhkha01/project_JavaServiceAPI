package com.example.courses.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.courses.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadImage(MultipartFile file) throws BadRequestException{
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Kiểm tra định dạng file
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File phải là hình ảnh",
                    Map.of("fileType", "Chỉ chấp nhận file hình ảnh (jpg, png, gif, etc.)"));
        }

        // Kiểm tra kích thước file (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File quá lớn",
                    Map.of("maxSize", "Kích thước file tối đa là 10MB"));
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "dishes",
                            "resource_type", "image"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new BadRequestException("Lỗi khi upload ảnh lên Cloudinary",
                    Map.of("error", e.getMessage()));
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        try {
            // Extract public_id from URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            // Log error but don't throw exception to avoid blocking the main operation
            System.err.println("Lỗi khi xóa ảnh từ Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        // Extract public_id from Cloudinary URL
        // Example: https://res.cloudinary.com/demo/image/upload/v1234567890/dishes/sample.jpg
        // public_id would be: dishes/sample

        if (!imageUrl.contains("cloudinary.com")) {
            return null;
        }

        String[] parts = imageUrl.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("upload".equals(parts[i]) && i + 2 < parts.length) {
                // Skip version if present (starts with 'v' followed by numbers)
                int startIndex = i + 1;
                if (parts[startIndex].matches("v\\d+")) {
                    startIndex++;
                }

                // Join remaining parts and remove file extension
                StringBuilder publicId = new StringBuilder();
                for (int j = startIndex; j < parts.length; j++) {
                    if (j > startIndex) {
                        publicId.append("/");
                    }
                    publicId.append(parts[j]);
                }

                String result = publicId.toString();
                // Remove file extension
                int lastDot = result.lastIndexOf('.');
                if (lastDot > 0) {
                    result = result.substring(0, lastDot);
                }
                return result;
            }
        }
        return null;
    }
}