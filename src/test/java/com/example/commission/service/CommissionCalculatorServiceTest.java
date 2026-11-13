package com.example.commission.service;

import com.example.commission.dto.QuoteModels.QuoteRequest;
import com.example.commission.dto.QuoteModels.QuoteResponse;
import com.example.commission.dto.QuoteModels.RiskBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CommissionCalculatorServiceTest {

    private CommissionCalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CommissionCalculatorService();
    }

    @Test
    void calculatesLowRiskShortTermQuoteAccurately() {
        QuoteRequest request = new QuoteRequest(new BigDecimal("10000"), 6, RiskBand.LOW);

        QuoteResponse response = calculatorService.calculate(request);

        assertNotNull(response.quoteId());
        assertEquals(new BigDecimal("0.015"), response.commissionRate());
        assertEquals(new BigDecimal("150.00"), response.totalCommission());
    }

    @Test
    void appliesTermAdderForMultiYearLoans() {
        // HIGH risk (4.0%) + 3 years (0.3%) = 4.3% (0.043)
        QuoteRequest request = new QuoteRequest(new BigDecimal("20000"), 36, RiskBand.HIGH);

        QuoteResponse response = calculatorService.calculate(request);

        assertEquals(new BigDecimal("0.043"), response.commissionRate());
        assertEquals(new BigDecimal("860.00"), response.totalCommission());
    }

    @Test
    void roundsTotalCommissionToTwoDecimalPlaces() {
        // MEDIUM risk (2.5%) on $333.33 = 8.33325 -> 8.33
        QuoteRequest request = new QuoteRequest(new BigDecimal("333.33"), 11, RiskBand.MEDIUM);

        QuoteResponse response = calculatorService.calculate(request);

        assertEquals(new BigDecimal("8.33"), response.totalCommission());
    }
}
