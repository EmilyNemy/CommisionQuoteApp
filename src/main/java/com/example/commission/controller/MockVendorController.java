package com.example.commission.controller;

import com.example.commission.dto.QuoteModels.QuoteRequest;
import com.example.commission.dto.QuoteModels.QuoteResponse;
import com.example.commission.service.CommissionCalculatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/external-vendor")
public class MockVendorController {

    private final CommissionCalculatorService calculatorService;
    private final String expectedApiKey;
    private final double failureRate;
    private final long latencyMs;
    private final Random random = new Random();

    public MockVendorController(
            CommissionCalculatorService calculatorService,
            @Value("${vendor.api.key}") String expectedApiKey,
            @Value("${vendor.failure.rate:0.25}") double failureRate,
            @Value("${vendor.latency.ms:500}") long latencyMs) {
        this.calculatorService = calculatorService;
        this.expectedApiKey = expectedApiKey;
        this.failureRate = failureRate;
        this.latencyMs = latencyMs;
    }

    @PostMapping("/commission")
    public ResponseEntity<?> calculateCommission(
            @RequestHeader(value = "api-key", required = false) String apiKey,
            @Valid @RequestBody QuoteRequest request) throws InterruptedException {

        // 1. Strict API key authentication
        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized: missing or invalid api-key header"));
        }

        // 2. Simulate realistic network delay
        if (latencyMs > 0) {
            Thread.sleep(latencyMs);
        }

        // 3. Simulate random transient failure
        if (random.nextDouble() < failureRate) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "External vendor temporarily unavailable"));
        }

        // 4. Generate and return quote
        QuoteResponse response = calculatorService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
