package com.lsb.webshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_logs")
@Getter
@Setter
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", length = 255, nullable = false)
    private String sessionId; // Mã định danh cuộc hội thoại

    // Liên kết với Người dùng (có thể null nếu là khách)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // nullable = true là mặc định
    private User user;

    @Column(length = 50, nullable = false)
    private String sender; // Ví dụ: "USER", "BOT"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(length = 100)
    private String intent; // Ý định mà Rasa nhận diện được

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false, nullable = false)
    private LocalDateTime timestamp;
}
