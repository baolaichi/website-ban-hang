package com.lsb.webshop.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RasaResponse {
    @JsonProperty("recipient_id")
    private String recipientId;
    private String text;
    // getters, setters
    public String getText() { return text; }
}
