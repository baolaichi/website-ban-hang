package com.lsb.webshop.repository;

import com.lsb.webshop.domain.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    List<ChatLog> findByUserIdOrderByTimestampAsc(Long userId);
    List<ChatLog> findBySessionIdOrderByTimestampAsc(String sessionId);

    // 2. Cho trang Quản lý Admin - Sắp xếp mới -> cũ
    // Sửa CreatedAt -> Timestamp
    List<ChatLog> findAllByOrderByTimestampDesc();

}
