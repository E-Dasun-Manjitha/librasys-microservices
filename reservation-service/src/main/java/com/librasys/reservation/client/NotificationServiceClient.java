package com.librasys.reservation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    @Value("${services.notification-service.api-key}")
    private String apiKey;

    public NotificationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendEmailNotification(String memberId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", memberId);
        body.put("message", message);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(
                notificationServiceUrl + "/api/notify/email",
                HttpMethod.POST, entity, Void.class);
    }
}
