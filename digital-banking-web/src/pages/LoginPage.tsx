import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';

const DEMO_CUSTOMERS = [
  { number: 'CUS-DEMO000001', name: 'Emma Thompson (Toronto)' },
  { number: 'CUS-DEMO000002', name: 'Noah Tremblay (Montreal)' },
  { number: 'CUS-DEMO000003', name: 'Priya Singh (Vancouver)' },
];

export function LoginPage() {
  const { login, loginWithEntra, isAuthenticated, authMode } = useAuth();
  const [username, setUsername] = useState('customer');
  const [password, setPassword] = useState('changeme-customer');
  const [customerNumber, setCustomerNumber] = useState('CUS-DEMO000001');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleBasicSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password, customerNumber);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Login failed. Check credentials and API.');
    } finally {
      setLoading(false);
    }
  }

  async function handleEntraLogin() {
    setError('');
    setLoading(true);
    try {
      await loginWithEntra();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Microsoft sign-in failed.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-panel">
        <div className="login-brand">
          <div className="brand-mark large">CB</div>
          <h1>Welcome to CloudBank</h1>
          <p>Secure digital banking for Canadian customers. All data shown is fictional.</p>
        </div>

        {authMode === 'entra' ? (
          <div className="login-form card">
            <h2>Sign in with Microsoft</h2>
            {error && <div className="alert error">{error}</div>}
            <p className="hint">
              Use your Microsoft Entra ID work account. Your email must match a seeded customer profile.
            </p>
            <button type="button" className="btn-primary" disabled={loading} onClick={handleEntraLogin}>
              {loading ? 'Signing in…' : 'Sign in with Microsoft'}
            </button>
          </div>
        ) : (
          <form className="login-form card" onSubmit={handleBasicSubmit}>
            <h2>Sign in</h2>
            {error && <div className="alert error">{error}</div>}
            <label>
              Username
              <input value={username} onChange={(e) => setUsername(e.target.value)} required />
            </label>
            <label>
              Password
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </label>
            <label>
              Demo customer profile
              <select
                value={customerNumber}
                onChange={(e) => setCustomerNumber(e.target.value)}
              >
                {DEMO_CUSTOMERS.map((c) => (
                  <option key={c.number} value={c.number}>
                    {c.name}
                  </option>
                ))}
              </select>
            </label>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
            <p className="hint">
              API must be running at <code>http://localhost:8080/api</code>
            </p>
          </form>
        )}
      </div>
    </div>
  );
}
