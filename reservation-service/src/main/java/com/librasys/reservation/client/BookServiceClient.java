package com.librasys.reservation.client;

import com.librasys.reservation.dto.BookDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BookServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.book-service.url}")
    private String bookServiceUrl;

    @Value("${services.book-service.api-key}")
    private String apiKey;

    public BookServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BookDto getBook(String bookId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<BookDto> response = restTemplate.exchange(
                bookServiceUrl + "/api/books/" + bookId,
                HttpMethod.GET, entity, BookDto.class);
        return response.getBody();
    }
}
