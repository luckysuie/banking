import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import type { Account } from '../api/types';
import { useAuth } from '../context/AuthContext';

function formatCad(amount: number) {
  return new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(amount);
}

export function AccountsPage() {
  const { customer } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!customer) return;
    apiRequest<Account[]>(`/accounts/customer/${customer.id}`)
      .then(setAccounts)
      .finally(() => setLoading(false));
  }, [customer]);

  if (loading) return <div className="page-loading">Loading accounts…</div>;

  return (
    <div className="page">
      <header className="page-header">
        <h2>Accounts</h2>
        <p>Chequing and savings accounts in CAD</p>
      </header>

      <div className="accounts-grid">
        {accounts.map((account) => (
          <article key={account.id} className="account-card card">
            <div className="account-card-top">
              <span className={`pill ${account.accountType.toLowerCase()}`}>
                {account.accountType}
              </span>
              <span className={`status ${account.accountStatus.toLowerCase()}`}>
                {account.accountStatus}
              </span>
            </div>
            <p className="mono account-number">{account.accountNumber}</p>
            <h3>{formatCad(account.availableBalance)}</h3>
            <p className="muted">Available balance</p>
            <dl className="meta-list">
              <div>
                <dt>Current</dt>
                <dd>{formatCad(account.currentBalance)}</dd>
              </div>
              <div>
                <dt>Daily limit</dt>
                <dd>{formatCad(account.dailyTransferLimit)}</dd>
              </div>
            </dl>
          </article>
        ))}
      </div>
    </div>
  );
}
