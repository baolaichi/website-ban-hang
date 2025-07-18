package com.lsb.webshop.service;

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
        String originalFilename = file.getOriginalFilename();
        log.info("[UploadService] Bắt đầu xử lý upload file: '{}', targetFolder: '{}'", originalFilename, targetFolder);

        if (file.isEmpty()) {
            log.warn("[UploadService] Tệp tin '{}' rỗng, upload bị hủy.", originalFilename);
            return "";
        }

        String finalName = "";
        String uploadBasePath = System.getProperty("user.dir") + "/uploads/images";
        String fullPath = uploadBasePath + File.separator + targetFolder;

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    log.info("[UploadService] Đã tạo thư mục: {}", dir.getAbsolutePath());
                } else {
                    log.warn("[UploadService] Không thể tạo thư mục: {}", dir.getAbsolutePath());
                }
            }

            finalName = System.currentTimeMillis() + "-" + originalFilename;
            File serverFile = new File(dir, finalName);

            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile))) {
                stream.write(file.getBytes());
            }

            log.info("[UploadService] Đã lưu thành công file '{}' tại '{}'", originalFilename, serverFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("[UploadService] Lỗi khi lưu file '{}': {}", originalFilename, e.getMessage(), e);
        }

        return finalName;
    }

   

}
