import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { apiRequest, clearStoredAuth, getBearerToken, getStoredAuth, setBearerToken, setStoredAuth } from '../api/client';
import type { Customer } from '../api/types';
import { loginRequest, msalInstance } from '../auth/msalConfig';
import { isEntraAuth } from '../config/env';

interface AuthState {
  customer: Customer | null;
  username: string;
  login: (username: string, password: string, customerNumber: string) => Promise<void>;
  loginWithEntra: () => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  authMode: 'basic' | 'entra';
}

const AuthContext = createContext<AuthState | null>(null);

const CUSTOMER_KEY = 'cloudbank_customer';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [customer, setCustomer] = useState<Customer | null>(() => {
    const raw = sessionStorage.getItem(CUSTOMER_KEY);
    return raw ? (JSON.parse(raw) as Customer) : null;
  });
  const [username, setUsername] = useState(() => sessionStorage.getItem('cloudbank_user') || '');

  useEffect(() => {
    if (isEntraAuth) {
      msalInstance.initialize();
    }
  }, []);

  const login = useCallback(async (user: string, password: string, customerNumber: string) => {
    setStoredAuth(user, password);
    const profile = await apiRequest<Customer>(`/customers/customer-number/${customerNumber}`);
    setCustomer(profile);
    setUsername(user);
    sessionStorage.setItem(CUSTOMER_KEY, JSON.stringify(profile));
    sessionStorage.setItem('cloudbank_user', user);
  }, []);

  const loginWithEntra = useCallback(async () => {
    await msalInstance.initialize();
    const result = await msalInstance.loginPopup(loginRequest);
    const token = result.accessToken;
    if (!token) {
      throw new Error('No access token returned from Microsoft Entra ID');
    }
    setBearerToken(token);
    const profile = await apiRequest<Customer>('/customers/me');
    setCustomer(profile);
    const name = result.account?.username ?? result.account?.name ?? 'entra-user';
    setUsername(name);
    sessionStorage.setItem(CUSTOMER_KEY, JSON.stringify(profile));
    sessionStorage.setItem('cloudbank_user', name);
  }, []);

  const logout = useCallback(async () => {
    if (isEntraAuth) {
      const accounts = msalInstance.getAllAccounts();
      if (accounts.length > 0) {
        await msalInstance.logoutPopup({ account: accounts[0] });
      }
    }
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
      loginWithEntra,
      logout,
      isAuthenticated: !!customer && (!!getStoredAuth() || !!getBearerToken()),
      authMode: isEntraAuth ? 'entra' as const : 'basic' as const,
    }),
    [customer, username, login, loginWithEntra, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
