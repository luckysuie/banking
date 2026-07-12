import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../api/client';
import type { Account, Notification } from '../api/types';
import { useAuth } from '../context/AuthContext';

function formatCad(amount: number) {
  return new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(amount);
}

export function DashboardPage() {
  const { customer } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!customer) return;
    Promise.all([
      apiRequest<Account[]>(`/accounts/customer/${customer.id}`),
      apiRequest<Notification[]>(`/notifications/customer/${customer.id}`),
    ])
      .then(([accts, notes]) => {
        setAccounts(accts);
        setNotifications(notes.slice(0, 5));
      })
      .finally(() => setLoading(false));
  }, [customer]);

  const totalBalance = accounts.reduce((sum, a) => sum + a.availableBalance, 0);

  if (loading) return <div className="page-loading">Loading your dashboard…</div>;

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Good day</p>
          <h2>{customer?.firstName}, here&apos;s your overview</h2>
        </div>
        <span className="badge">Fictional demo data</span>
      </header>

      <section className="stats-grid">
        <article className="stat-card highlight">
          <p>Total available</p>
          <h3>{formatCad(totalBalance)}</h3>
          <small>{accounts.length} active accounts</small>
        </article>
        <article className="stat-card">
          <p>Customer number</p>
          <h3 className="mono">{customer?.customerNumber}</h3>
        </article>
        <article className="stat-card">
          <p>Unread alerts</p>
          <h3>{notifications.filter((n) => !n.read).length}</h3>
        </article>
      </section>

      <section className="two-col">
        <div className="card">
          <div className="card-header">
            <h3>Your accounts</h3>
            <Link to="/accounts">View all</Link>
          </div>
          <ul className="account-list">
            {accounts.map((account) => (
              <li key={account.id}>
                <div>
                  <strong>{account.accountType}</strong>
                  <span className="mono">{account.accountNumber}</span>
                </div>
                <span className="amount">{formatCad(account.availableBalance)}</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="card">
          <div className="card-header">
            <h3>Recent alerts</h3>
            <Link to="/notifications">View all</Link>
          </div>
          <ul className="notification-list">
            {notifications.length === 0 && <li className="empty">No notifications yet.</li>}
            {notifications.map((n) => (
              <li key={n.id} className={n.read ? '' : 'unread'}>
                <span className="tag">{n.type.replace('_', ' ')}</span>
                <p>{n.message}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="quick-actions">
        <Link to="/transfer" className="action-tile">Send money</Link>
        <Link to="/beneficiaries" className="action-tile">Manage payees</Link>
        <Link to="/transactions" className="action-tile">View history</Link>
      </section>
    </div>
  );
}
