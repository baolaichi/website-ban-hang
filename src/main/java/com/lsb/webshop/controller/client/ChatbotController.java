package com.lsb.webshop.controller.client;



import com.lsb.webshop.domain.ChatLog;
import com.lsb.webshop.domain.User;
import com.lsb.webshop.repository.ChatLogRepository;
import com.lsb.webshop.service.RasaService;
import com.lsb.webshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// DTO cho request từ Frontend
class ChatRequest {
    public String message;
    public String sessionId;
}

@RestController
@RequestMapping("/api/chat")
@CrossOrigin // Cho phép frontend (từ domain khác) gọi API này
public class ChatbotController {

    @Autowired
    private RasaService rasaService;

    @Autowired
    private ChatLogRepository chatLogRepository; // Tiêm repo để ghi log

    @Autowired
    private UserService userService;

    @PostMapping("/send")
    public ResponseEntity<?> handleChatMessage(@RequestBody ChatRequest chatRequest) {

        // 1. Ghi log tin nhắn của USER
        ChatLog userLog = new ChatLog();
        userLog.setSessionId(chatRequest.sessionId);
        userLog.setSender("USER");
        userLog.setMessage(chatRequest.message);
        userLog.setTimestamp(LocalDateTime.now());
        // (Nếu user đã đăng nhập, bạn có thể lấy user từ Spring Security và setUser(user))
        chatLogRepository.save(userLog);

        // 2. Gọi Rasa Service để lấy câu trả lời
        String botResponseText = rasaService.getRasaResponse(chatRequest.message, chatRequest.sessionId);

        // 3. Ghi log tin nhắn của BOT
        ChatLog botLog = new ChatLog();
        botLog.setSessionId(chatRequest.sessionId);
        botLog.setSender("BOT");
        botLog.setMessage(botResponseText);
        botLog.setTimestamp(LocalDateTime.now());
        // (Bạn cũng có thể lấy intent từ Rasa trả về để lưu, nhưng cần DTO phức tạp hơn)
        chatLogRepository.save(botLog);

        // 4. Trả về cho Frontend
        // Trả về JSON: { "response": "Đây là câu trả lời..." }
        return ResponseEntity.ok(Map.of("response", botResponseText));
    }

    // ===== API MỚI: LẤY LỊCH SỬ CHAT =====
    @GetMapping("/history")
    public ResponseEntity<List<ChatLog>> getChatHistory(@RequestParam(required = false) String sessionId) {
        User currentUser = userService.getCurrentUser();
        List<ChatLog> history;

        if (currentUser != null) {
            // SỬA: Gọi hàm theo Timestamp
            history = chatLogRepository.findByUserIdOrderByTimestampAsc(currentUser.getId());
        } else {
            // SỬA: Gọi hàm theo Timestamp
            history = chatLogRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        }

        return ResponseEntity.ok(history);
    }
}