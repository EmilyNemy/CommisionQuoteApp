package com.example.commission.controller;

import com.example.commission.dto.QuoteModels.QuoteRequest;
import com.example.commission.dto.QuoteModels.QuoteResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@RestController
@RequestMapping("/api/quotes")
@CrossOrigin(origins = "http://localhost:5173")
public class QuoteController {

    private final RestClient restClient;
    private final String vendorUrl;
    private final String vendorApiKey;

    public QuoteController(
            RestClient.Builder restClientBuilder,
            @Value("${vendor.api.url}") String vendorUrl,
            @Value("${vendor.api.key}") String vendorApiKey) {
        this.restClient = restClientBuilder.build();
        this.vendorUrl = vendorUrl;
        this.vendorApiKey = vendorApiKey;
    }

    @PostMapping
    public ResponseEntity<?> generateQuote(@Valid @RequestBody QuoteRequest request) {
        try {
            QuoteResponse response = restClient.post()
                    .uri(vendorUrl)
                    .header("api-key", vendorApiKey)
                    .body(request)
                    .retrieve()
                    .body(QuoteResponse.class);

            return ResponseEntity.ok(response);

        } catch (RestClientException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Unable to generate quote from vendor. Please try again."));
        }
    }
}
