package com.example.permission.controller;

import com.example.permission.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/common")
public class UploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Value("${upload.max-size:5242880}")
    private Long maxSize;

    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    ));

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            return Result.error("只支持 jpg、png、gif、webp 格式的图片");
        }

        if (file.getSize() > maxSize) {
            return Result.error("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        try {
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            File uploadDir = new File(uploadPath + File.separator + datePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            Path filePath = Paths.get(uploadDir.getAbsolutePath(), newFilename);
            Files.write(filePath, file.getBytes());

            String fileUrl = "/uploads/" + datePath + "/" + newFilename;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("name", originalFilename);
            result.put("size", String.valueOf(file.getSize()));

            return Result.success("上传成功", result);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
