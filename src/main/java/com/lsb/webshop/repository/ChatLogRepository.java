package com.lsb.webshop.repository;

import com.lsb.webshop.domain.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
}
