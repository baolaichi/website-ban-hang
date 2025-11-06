package com.lsb.webshop.domain.dto;

import lombok.Data;

@Data
public class RasaRequest {
    private String message;
    private String sender; // sender chính là sessionId
    // constructor, getters, setters
    public RasaRequest(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }
}
