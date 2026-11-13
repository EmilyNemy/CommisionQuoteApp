package com.example.commission.service;

import com.example.commission.dto.QuoteModels.QuoteRequest;
import com.example.commission.dto.QuoteModels.QuoteResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class CommissionCalculatorService {

    public QuoteResponse calculate(QuoteRequest request) {
        BigDecimal baseRate = switch (request.riskBand()) {
            case LOW -> new BigDecimal("0.015");    // 1.5%
            case MEDIUM -> new BigDecimal("0.025"); // 2.5%
            case HIGH -> new BigDecimal("0.040");   // 4.0%
        };

        // Add 0.1% for every full 12-month period
        int years = request.loanTermInMonths() / 12;
        BigDecimal termAdder = BigDecimal.valueOf(years).multiply(new BigDecimal("0.001"));

        BigDecimal finalRate = baseRate.add(termAdder);
        BigDecimal totalCommission = request.loanAmount()
                .multiply(finalRate)
                .setScale(2, RoundingMode.HALF_UP);

        return new QuoteResponse(
                UUID.randomUUID().toString(),
                finalRate,
                totalCommission
        );
    }
}
