import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import type { Notification } from '../api/types';
import { useAuth } from '../context/AuthContext';

function formatDate(iso: string) {
  return new Intl.DateTimeFormat('en-CA', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(iso));
}

export function NotificationsPage() {
  const { customer } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  function load() {
    if (!customer) return;
    apiRequest<Notification[]>(`/notifications/customer/${customer.id}`)
      .then(setNotifications)
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, [customer]);

  async function markRead(id: string) {
    await apiRequest<Notification>(`/notifications/${id}/read`, { method: 'PATCH' });
    load();
  }

  if (loading) return <div className="page-loading">Loading notifications…</div>;

  return (
    <div className="page">
      <header className="page-header">
        <h2>Alerts & notifications</h2>
        <p>Account activity and payment updates</p>
      </header>

      <ul className="notification-feed">
        {notifications.length === 0 && (
          <li className="card empty">No notifications yet.</li>
        )}
        {notifications.map((n) => (
          <li key={n.id} className={`card notification-item ${n.read ? '' : 'unread'}`}>
            <div className="notification-head">
              <span className="tag">{n.type.replace(/_/g, ' ')}</span>
              <time>{formatDate(n.createdAt)}</time>
            </div>
            <p>{n.message}</p>
            {!n.read && (
              <button type="button" className="btn-ghost" onClick={() => markRead(n.id)}>
                Mark as read
              </button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
