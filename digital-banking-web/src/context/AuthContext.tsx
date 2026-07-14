import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { apiRequest, clearStoredAuth, getStoredAuth, setStoredAuth } from '../api/client';
import type { Customer } from '../api/types';

interface AuthState {
  customer: Customer | null;
  username: string;
  login: (username: string, password: string, customerNumber: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthState | null>(null);

const CUSTOMER_KEY = 'cloudbank_customer';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [customer, setCustomer] = useState<Customer | null>(() => {
    const raw = sessionStorage.getItem(CUSTOMER_KEY);
    return raw ? (JSON.parse(raw) as Customer) : null;
  });
  const [username, setUsername] = useState(() => sessionStorage.getItem('cloudbank_user') || '');

  const login = useCallback(async (user: string, password: string, customerNumber: string) => {
    setStoredAuth(user, password);
    const profile = await apiRequest<Customer>(`/customers/customer-number/${customerNumber}`);
    setCustomer(profile);
    setUsername(user);
    sessionStorage.setItem(CUSTOMER_KEY, JSON.stringify(profile));
    sessionStorage.setItem('cloudbank_user', user);
  }, []);

  const logout = useCallback(() => {
    clearStoredAuth();
    sessionStorage.removeItem(CUSTOMER_KEY);
    sessionStorage.removeItem('cloudbank_user');
    setCustomer(null);
    setUsername('');
  }, []);

  const value = useMemo(
    () => ({
      customer,
      username,
      login,
      logout,
      isAuthenticated: !!customer && !!getStoredAuth(),
    }),
    [customer, username, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
