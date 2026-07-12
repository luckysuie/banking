import { useEffect, useState } from 'react';
import { apiPageRequest, apiRequest } from '../api/client';
import type { Account, Transaction } from '../api/types';
import { useAuth } from '../context/AuthContext';

function formatCad(amount: number) {
  return new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(amount);
}

function formatDate(iso: string) {
  return new Intl.DateTimeFormat('en-CA', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(iso));
}

export function TransactionsPage() {
  const { customer } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [accountId, setAccountId] = useState('');
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!customer) return;
    apiRequest<Account[]>(`/accounts/customer/${customer.id}`)
      .then((accts) => {
        setAccounts(accts);
        if (accts[0]) setAccountId(accts[0].id);
      })
      .finally(() => setLoading(false));
  }, [customer]);

  useEffect(() => {
    if (!accountId) return;
    apiPageRequest<Transaction>(`/transactions/account/${accountId}?size=20`)
      .then((page) => setTransactions(page.content));
  }, [accountId]);

  if (loading) return <div className="page-loading">Loading transactions…</div>;

  return (
    <div className="page">
      <header className="page-header">
        <h2>Transaction history</h2>
        <label className="inline-select">
          Account
          <select value={accountId} onChange={(e) => setAccountId(e.target.value)}>
            {accounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.accountType} — {a.accountNumber}
              </option>
            ))}
          </select>
        </label>
      </header>

      <div className="card table-card">
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Type</th>
              <th>Description</th>
              <th>Amount</th>
              <th>Balance after</th>
            </tr>
          </thead>
          <tbody>
            {transactions.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">No transactions for this account.</td>
              </tr>
            )}
            {transactions.map((tx) => (
              <tr key={tx.id}>
                <td>{formatDate(tx.createdAt)}</td>
                <td>
                  <span className={`pill small ${tx.transactionType.toLowerCase()}`}>
                    {tx.transactionType}
                  </span>
                </td>
                <td>{tx.description || tx.transactionReference}</td>
                <td className={tx.transactionType === 'DEBIT' ? 'debit' : 'credit'}>
                  {tx.transactionType === 'DEBIT' ? '−' : '+'}
                  {formatCad(tx.amount)}
                </td>
                <td>{formatCad(tx.balanceAfter)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
