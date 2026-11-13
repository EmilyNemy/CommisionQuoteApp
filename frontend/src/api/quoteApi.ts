export type RiskBand = 'LOW' | 'MEDIUM' | 'HIGH';

export interface QuoteRequest {
    loanAmount: number;
    loanTermInMonths: number;
    riskBand: RiskBand;
}

export interface QuoteResponse {
    quoteId: string;
    commissionRate: number;
    totalCommission: number;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function generateCommissionQuote(payload: QuoteRequest): Promise<QuoteResponse> {
    const response = await fetch(`${API_BASE_URL}/api/quotes`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(data.error || 'Failed to generate commission quote');
    }

    return data as QuoteResponse;
}
