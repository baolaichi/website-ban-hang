package com.lsb.webshop.controller.admin;

import com.lsb.webshop.domain.ChatLog;
import com.lsb.webshop.repository.ChatLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/chat-logs")
public class AdminChatLogController extends BaseController{

    private final ChatLogRepository chatLogRepository;

    public AdminChatLogController(ChatLogRepository chatLogRepository) {
        this.chatLogRepository = chatLogRepository;
    }

    @GetMapping
    public String getChatLogsPage(Model model) {
        // Lấy tất cả tin nhắn, tin mới nhất lên đầu
        List<ChatLog> logs = chatLogRepository.findAllByOrderByTimestampDesc();

        model.addAttribute("chatLogs", logs);
        model.addAttribute("title", "Lịch sử Chatbot");

        return "admin/chat/show";
    }
}