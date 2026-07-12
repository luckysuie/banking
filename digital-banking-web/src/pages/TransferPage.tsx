import { useEffect, useState } from 'react';
import { ApiError, apiRequest } from '../api/client';
import type { Account, Beneficiary, Payment } from '../api/types';
import { useAuth } from '../context/AuthContext';

export function TransferPage() {
  const { customer } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([]);
  const [sourceAccountId, setSourceAccountId] = useState('');
  const [beneficiaryId, setBeneficiaryId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState<Payment | null>(null);

  useEffect(() => {
    if (!customer) return;
    Promise.all([
      apiRequest<Account[]>(`/accounts/customer/${customer.id}`),
      apiRequest<Beneficiary[]>(`/beneficiaries/customer/${customer.id}`),
    ])
      .then(([accts, bene]) => {
        setAccounts(accts);
        setBeneficiaries(bene);
        if (accts[0]) setSourceAccountId(accts[0].id);
        if (bene[0]) setBeneficiaryId(bene[0].id);
      })
      .finally(() => setLoading(false));
  }, [customer]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess(null);
    setSubmitting(true);
    try {
      const payment = await apiRequest<Payment>('/payments/transfers', {
        method: 'POST',
        body: JSON.stringify({
          sourceAccountId,
          beneficiaryId,
          amount: parseFloat(amount),
          description: description || undefined,
          idempotencyKey: `ui-${crypto.randomUUID()}`,
        }),
      });
      setSuccess(payment);
      setAmount('');
      setDescription('');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Transfer failed');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className="page-loading">Loading transfer options…</div>;

  return (
    <div className="page">
      <header className="page-header">
        <h2>Send money</h2>
        <p>Transfer funds to a registered payee</p>
      </header>

      <form className="card form-card" onSubmit={handleSubmit}>
        {error && <div className="alert error">{error}</div>}
        {success && (
          <div className="alert success">
            Transfer {success.paymentReference} completed — {success.amount} {success.currency}
          </div>
        )}

        <label>
          From account
          <select
            value={sourceAccountId}
            onChange={(e) => setSourceAccountId(e.target.value)}
            required
          >
            {accounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.accountType} — {a.accountNumber} (${a.availableBalance})
              </option>
            ))}
          </select>
        </label>

        <label>
          To payee
          <select
            value={beneficiaryId}
            onChange={(e) => setBeneficiaryId(e.target.value)}
            required
          >
            {beneficiaries.map((b) => (
              <option key={b.id} value={b.id}>
                {b.beneficiaryName} — {b.beneficiaryAccountNumber}
              </option>
            ))}
          </select>
        </label>

        <label>
          Amount (CAD)
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </label>

        <label>
          Description (optional)
          <input
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g. Rent payment"
          />
        </label>

        <button type="submit" className="btn-primary" disabled={submitting}>
          {submitting ? 'Processing…' : 'Send transfer'}
        </button>
      </form>
    </div>
  );
}
