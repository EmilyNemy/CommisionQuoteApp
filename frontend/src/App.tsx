import React, { useState } from 'react';
import { generateCommissionQuote, type QuoteResponse, type RiskBand } from './api/quoteApi';
import './App.css';

export default function App() {
  const [loanAmount, setLoanAmount] = useState<string>('25000');
  const [loanTermInMonths, setLoanTermInMonths] = useState<string>('36');
  const [riskBand, setRiskBand] = useState<RiskBand>('LOW');

  const [quote, setQuote] = useState<QuoteResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const requestQuote = async () => {
    setError(null);
    setQuote(null);
    setIsLoading(true);

    try {
      const result = await generateCommissionQuote({
        loanAmount: Number(loanAmount),
        loanTermInMonths: Number(loanTermInMonths),
        riskBand,
      });
      setQuote(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await requestQuote();
  };

  const formatCurrency = (amount: number) =>
      new Intl.NumberFormat('en-AU', { style: 'currency', currency: 'AUD' }).format(amount);

  return (
      <main className="container">
        <h1>Commission Quote Generator</h1>
        <p className="subtitle">Enter loan details to calculate an instant broker commission quote.</p>

        <form onSubmit={handleSubmit} className="quote-form">
          <div className="form-group">
            <label htmlFor="loanAmount">Loan Amount ($)</label>
            <input
                id="loanAmount"
                type="number"
                min="1"
                step="any"
                required
                value={loanAmount}
                onChange={(e) => setLoanAmount(e.target.value)}
                disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="loanTermInMonths">Loan Term (Months)</label>
            <input
                id="loanTermInMonths"
                type="number"
                min="1"
                step="1"
                required
                value={loanTermInMonths}
                onChange={(e) => setLoanTermInMonths(e.target.value)}
                disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="riskBand">Risk Band</label>
            <select
                id="riskBand"
                value={riskBand}
                onChange={(e) => setRiskBand(e.target.value as RiskBand)}
                disabled={isLoading}
            >
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
            </select>
          </div>

          <button type="submit" disabled={isLoading} className="submit-btn">
            {isLoading ? 'Generating Quote...' : 'Generate Quote'}
          </button>
        </form>

        {error && (
            <div role="alert" className="error-banner">
              <p>{error}</p>
              <button type="button" onClick={requestQuote} disabled={isLoading} className="retry-btn">
                Retry
              </button>
            </div>
        )}

        {quote && (
            <section aria-label="Quote Result" className="quote-card">
              <h2>Generated Quote</h2>
              <dl className="quote-details">
                <div>
                  <dt>Quote ID</dt>
                  <dd>{quote.quoteId}</dd>
                </div>
                <div>
                  <dt>Commission Rate</dt>
                  <dd>{(quote.commissionRate * 100).toFixed(2)}%</dd>
                </div>
                <div>
                  <dt>Total Commission</dt>
                  <dd className="highlight">{formatCurrency(quote.totalCommission)}</dd>
                </div>
              </dl>
            </section>
        )}
      </main>
  );
}
