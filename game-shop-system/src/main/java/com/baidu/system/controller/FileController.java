package com.baidu.system.controller;

import com.baidu.common.ApiResponse;
import com.baidu.security.RequireRoles;
import com.baidu.security.Role;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload.base-dir:uploads}")
    private String uploadBaseDir;

    private Path uploadRoot;

    @PostConstruct
    public void init() {
        this.uploadRoot = Paths.get(uploadBaseDir);
    }

    @RequireRoles({Role.ADMIN, Role.MERCHANT})
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException();
        }
        String contentType = file.getContentType();
        LocalDate d = LocalDate.now();
        Path dir = uploadRoot.resolve(Paths.get(String.valueOf(d.getYear()), String.format("%02d", d.getMonthValue())));
        Files.createDirectories(dir);

        String ori = file.getOriginalFilename();
        String ext = "";
        if (ori != null) {
            String cleaned = StringUtils.cleanPath(ori);
            int idx = cleaned.lastIndexOf('.');
            if (idx >= 0) ext = cleaned.substring(idx).toLowerCase();
        }
        boolean isImageByMime = contentType != null && contentType.toLowerCase().startsWith("image/");
        boolean isImageByExt = ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp|svg|ico)$");
        if (!isImageByMime && !isImageByExt) {
            throw new IllegalArgumentException();
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = dir.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String url = "/uploads/" + d.getYear() + "/" + String.format("%02d", d.getMonthValue()) + "/" + name;
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        return ApiResponse.ok(data);
    }
}
