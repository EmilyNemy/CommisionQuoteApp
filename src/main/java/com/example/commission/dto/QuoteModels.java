package com.example.commission.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class QuoteModels {

    public enum RiskBand {
        LOW(new BigDecimal("0.015")),    // 1.5%
        MEDIUM(new BigDecimal("0.025")), // 2.5%
        HIGH(new BigDecimal("0.040"));   // 4.0%

        private final BigDecimal baseRate;

        RiskBand(BigDecimal baseRate) {
            this.baseRate = baseRate;
        }

        public BigDecimal getBaseRate() {
            return baseRate;
        }
    }

    public record QuoteRequest(
            @NotNull(message = "Loan amount is required")
            @Positive(message = "Loan amount must be greater than zero")
            BigDecimal loanAmount,

            @NotNull(message = "Loan term is required")
            @Min(value = 1, message = "Loan term must be at least 1 month")
            Integer loanTermInMonths,

            @NotNull(message = "Risk band is required")
            RiskBand riskBand
    ) {}

    public record QuoteResponse(
            String quoteId,
            BigDecimal commissionRate,
            BigDecimal totalCommission
    ) {}
}
