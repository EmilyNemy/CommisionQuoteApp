package com.example.commission.controller;

import com.example.commission.service.CommissionCalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = MockVendorController.class,
        properties = {
                "vendor.api.key=test-secret-key",
                "vendor.failure.rate=0.0",
                "vendor.latency.ms=0"
        }
)
@Import(CommissionCalculatorService.class)
class MockVendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final String validPayload = """
            {
                "loanAmount": 15000,
                "loanTermInMonths": 24,
                "riskBand": "MEDIUM"
            }
            """;

    @Test
    void returns401WhenApiKeyIsMissing() throws Exception {
        mockMvc.perform(post("/external-vendor/commission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void returns401WhenApiKeyIsInvalid() throws Exception {
        mockMvc.perform(post("/external-vendor/commission")
                        .header("api-key", "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns400WhenRequestPayloadIsInvalid() throws Exception {
        String invalidPayload = """
                {
                    "loanAmount": -500,
                    "loanTermInMonths": 0,
                    "riskBand": "LOW"
                }
                """;

        mockMvc.perform(post("/external-vendor/commission")
                        .header("api-key", "test-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns200AndQuoteWhenAuthorizedAndValid() throws Exception {
        mockMvc.perform(post("/external-vendor/commission")
                        .header("api-key", "test-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").isNotEmpty())
                .andExpect(jsonPath("$.commissionRate").value(0.027))
                .andExpect(jsonPath("$.totalCommission").value(405.00));
    }
}
