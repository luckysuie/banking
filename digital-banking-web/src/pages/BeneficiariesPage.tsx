import { useEffect, useState } from 'react';
import { ApiError, apiRequest } from '../api/client';
import type { Beneficiary } from '../api/types';
import { useAuth } from '../context/AuthContext';

export function BeneficiariesPage() {
  const { customer } = useAuth();
  const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    beneficiaryName: '',
    beneficiaryAccountNumber: '',
    bankName: 'Fictional Canadian Bank',
    transitNumber: '12345',
    institutionNumber: '001',
    nickname: '',
  });

  function load() {
    if (!customer) return;
    apiRequest<Beneficiary[]>(`/beneficiaries/customer/${customer.id}`)
      .then(setBeneficiaries)
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, [customer]);

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await apiRequest<Beneficiary>('/beneficiaries', {
        method: 'POST',
        body: JSON.stringify({ customerId: customer?.id, ...form }),
      });
      setShowForm(false);
      setForm({
        beneficiaryName: '',
        beneficiaryAccountNumber: '',
        bankName: 'Fictional Canadian Bank',
        transitNumber: '12345',
        institutionNumber: '001',
        nickname: '',
      });
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not add payee');
    }
  }

  if (loading) return <div className="page-loading">Loading payees…</div>;

  return (
    <div className="page">
      <header className="page-header row">
        <div>
          <h2>Payees</h2>
          <p>People and businesses you can pay</p>
        </div>
        <button type="button" className="btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Add payee'}
        </button>
      </header>

      {showForm && (
        <form className="card form-card" onSubmit={handleAdd}>
          {error && <div className="alert error">{error}</div>}
          <label>
            Name
            <input
              value={form.beneficiaryName}
              onChange={(e) => setForm({ ...form, beneficiaryName: e.target.value })}
              required
            />
          </label>
          <label>
            Account number (7–12 digits)
            <input
              value={form.beneficiaryAccountNumber}
              onChange={(e) => setForm({ ...form, beneficiaryAccountNumber: e.target.value })}
              pattern="\d{7,12}"
              required
            />
          </label>
          <label>
            Bank name
            <input
              value={form.bankName}
              onChange={(e) => setForm({ ...form, bankName: e.target.value })}
              required
            />
          </label>
          <div className="form-row">
            <label>
              Transit (5 digits)
              <input
                value={form.transitNumber}
                onChange={(e) => setForm({ ...form, transitNumber: e.target.value })}
                pattern="\d{5}"
                required
              />
            </label>
            <label>
              Institution (3 digits)
              <input
                value={form.institutionNumber}
                onChange={(e) => setForm({ ...form, institutionNumber: e.target.value })}
                pattern="\d{3}"
                required
              />
            </label>
          </div>
          <label>
            Nickname (optional)
            <input
              value={form.nickname}
              onChange={(e) => setForm({ ...form, nickname: e.target.value })}
            />
          </label>
          <button type="submit" className="btn-primary">Save payee</button>
        </form>
      )}

      <div className="beneficiary-grid">
        {beneficiaries.map((b) => (
          <article key={b.id} className="card beneficiary-card">
            <h3>{b.beneficiaryName}</h3>
            {b.nickname && <p className="muted">Nickname: {b.nickname}</p>}
            <p className="mono">{b.beneficiaryAccountNumber}</p>
            <p className="muted">{b.bankName}</p>
            <p className="muted">
              Transit {b.transitNumber} · Institution {b.institutionNumber}
            </p>
            <span className={`pill small ${b.status.toLowerCase()}`}>{b.status}</span>
          </article>
        ))}
      </div>
    </div>
  );
}
