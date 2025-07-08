package com.lsb.web_shop.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UploadService {
    private final ServletContext servletContext;

    public UploadService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String HandleSaveUploadFile(MultipartFile file, String targetFolder) {
        log.info("Gọi HandleSaveUploadFile với file: {}, targetFolder: {}", 
                file.getOriginalFilename(), targetFolder);

        if (file.isEmpty()) {
            log.warn("File upload rỗng!");
            return "";
        }

        byte[] bytes;
        String rootPath = this.servletContext.getRealPath("/resources/images");
        String finalName = "";

        try {
            bytes = file.getBytes();
            File dir = new File(rootPath + File.separator + targetFolder);

            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                log.info("Tạo thư mục upload: {} - Thành công: {}", dir.getAbsolutePath(), created);
            }

            finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            File serverFile = new File(dir.getAbsolutePath() + File.separator + finalName);

            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile))) {
                stream.write(bytes);
            }

            log.info("Đã lưu file tại: {}", serverFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("Lỗi khi lưu file upload: {}", e.getMessage(), e);
        }

        return finalName;
    }
}
