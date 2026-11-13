package com.example.commission.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.restclient.test.autoconfigure.AutoConfigureMockRestServiceServer;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = QuoteController.class,
        properties = {
                "vendor.api.url=http://mock-vendor/commission",
                "vendor.api.key=bff-secret-key"
        }
)
@ImportAutoConfiguration(RestClientAutoConfiguration.class)
@AutoConfigureMockRestServiceServer
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockRestServiceServer mockServer;

    @BeforeEach
    void resetServer() {
        mockServer.reset();
    }

    @Test
    void forwardsRequestWithSecretApiKeyAndReturnsQuote() throws Exception {
        String requestJson = """
                {
                    "loanAmount": 10000,
                    "loanTermInMonths": 12,
                    "riskBand": "LOW"
                }
                """;

        String vendorResponseJson = """
                {
                    "quoteId": "q-123",
                    "commissionRate": 0.016,
                    "totalCommission": 160.00
                }
                """;

        mockServer.expect(requestTo("http://mock-vendor/commission"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "bff-secret-key"))
                .andRespond(withSuccess(vendorResponseJson, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").value("q-123"))
                .andExpect(jsonPath("$.totalCommission").value(160.00));

        mockServer.verify();
    }

    @Test
    void returns502BadGatewayWhenVendorFails() throws Exception {
        String requestJson = """
                {
                    "loanAmount": 10000,
                    "loanTermInMonths": 12,
                    "riskBand": "LOW"
                }
                """;

        mockServer.expect(requestTo("http://mock-vendor/commission"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").exists());
    }
}
