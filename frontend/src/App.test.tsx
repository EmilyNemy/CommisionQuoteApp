// @vitest-environment jsdom
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import App from './App';
import * as quoteApi from './api/quoteApi';

vi.mock('./api/quoteApi');

describe('Commission Quote Generator App', () => {
    beforeEach(() => {
        vi.resetAllMocks();
    });

    it('submits loan details and displays the generated quote', async () => {
        const user = userEvent.setup();
        vi.spyOn(quoteApi, 'generateCommissionQuote').mockResolvedValueOnce({
            quoteId: 'quote-98765',
            commissionRate: 0.028,
            totalCommission: 1400.0,
        });

        render(<App />);

        const amountInput = screen.getByLabelText(/loan amount/i);
        const termInput = screen.getByLabelText(/loan term/i);
        const riskSelect = screen.getByLabelText(/risk band/i);

        await user.clear(amountInput);
        await user.type(amountInput, '50000');
        await user.clear(termInput);
        await user.type(termInput, '36');
        await user.selectOptions(riskSelect, 'MEDIUM');

        await user.click(screen.getByRole('button', { name: /generate quote/i }));

        expect(quoteApi.generateCommissionQuote).toHaveBeenCalledWith({
            loanAmount: 50000,
            loanTermInMonths: 36,
            riskBand: 'MEDIUM',
        });

        expect(await screen.findByText('q-98765')).toBeInTheDocument();
        expect(screen.getByText('2.80%')).toBeInTheDocument();
        expect(screen.getByText('$1,400.00')).toBeInTheDocument();
    });

    it('shows a loading state while fetching the quote', async () => {
        const user = userEvent.setup();
        let resolvePromise!: (value: quoteApi.QuoteResponse) => void;

        vi.spyOn(quoteApi, 'generateCommissionQuote').mockImplementationOnce(
            () => new Promise((resolve) => { resolvePromise = resolve; })
        );

        render(<App />);

        const button = screen.getByRole('button', { name: /generate quote/i });
        await user.click(button);

        expect(screen.getByRole('button', { name: /generating quote/i })).toBeDisabled();

        resolvePromise({
            quoteId: 'quote-111',
            commissionRate: 0.015,
            totalCommission: 375.0,
        });

        await waitFor(() => {
            expect(screen.getByRole('button', { name: /generate quote/i })).toBeEnabled();
        });
    });

    it('displays an error message when the API fails and allows retry', async () => {
        const user = userEvent.setup();
        const mockFn = vi.spyOn(quoteApi, 'generateCommissionQuote')
            .mockRejectedValueOnce(new Error('Vendor service temporarily unavailable'))
            .mockResolvedValueOnce({
                quoteId: 'q-retry-ok',
                commissionRate: 0.018,
                totalCommission: 450.0,
            });

        render(<App />);

        await user.click(screen.getByRole('button', { name: /generate quote/i }));

        expect(await screen.findByRole('alert')).toHaveTextContent(
            'Vendor service temporarily unavailable'
        );

        await user.click(screen.getByRole('button', { name: /retry/i }));

        expect(mockFn).toHaveBeenCalledTimes(2);
        expect(await screen.findByText('q-ry-ok')).toBeInTheDocument();
    });
});
