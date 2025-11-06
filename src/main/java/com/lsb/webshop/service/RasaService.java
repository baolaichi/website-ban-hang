package com.lsb.webshop.service;


import com.lsb.webshop.domain.dto.RasaRequest;
import com.lsb.webshop.domain.dto.RasaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
// (Import các DTO bạn vừa tạo)

@Service
public class RasaService {

    @Value("${rasa.server.url}")
    private String RASA_API_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getRasaResponse(String message, String sessionId) {
        RasaRequest request = new RasaRequest(message, sessionId);

        try {
            // Gửi POST request đến Rasa Server
            RasaResponse[] responses = restTemplate.postForObject(RASA_API_URL, request, RasaResponse[].class);

            if (responses != null && responses.length > 0) {
                return responses[0].getText(); // Lấy câu trả lời
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Rasa Server: " + e.getMessage());
            return "Bot đang bảo trì, vui lòng thử lại sau."; // Trả về thông báo lỗi
        }
        return "Xin lỗi, tôi không hiểu ý bạn.";
    }
}
