import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { to: '/', label: 'Dashboard', icon: '◉' },
  { to: '/accounts', label: 'Accounts', icon: '▣' },
  { to: '/transfer', label: 'Transfer', icon: '⇄' },
  { to: '/beneficiaries', label: 'Payees', icon: '◎' },
  { to: '/transactions', label: 'History', icon: '☰' },
  { to: '/notifications', label: 'Alerts', icon: '✉' },
];

export function Layout() {
  const { customer, username, logout } = useAuth();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">CB</div>
          <div>
            <h1>CloudBank</h1>
            <p>Digital Banking</p>
          </div>
        </div>

        <nav className="nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-chip">
            <span className="user-avatar">
              {customer?.firstName?.[0]}
              {customer?.lastName?.[0]}
            </span>
            <div>
              <strong>{customer?.firstName} {customer?.lastName}</strong>
              <small>{username}</small>
            </div>
          </div>
          <button type="button" className="btn-ghost" onClick={logout}>
            Sign out
          </button>
        </div>
      </aside>

      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
