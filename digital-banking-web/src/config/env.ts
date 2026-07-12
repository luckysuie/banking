export type AuthMode = 'basic' | 'entra';

export const authMode = (import.meta.env.VITE_AUTH_MODE ?? 'basic') as AuthMode;
export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api';
export const entraTenantId = import.meta.env.VITE_ENTRA_TENANT_ID ?? '';
export const entraClientId = import.meta.env.VITE_ENTRA_CLIENT_ID ?? '';
export const entraApiScope = import.meta.env.VITE_ENTRA_API_SCOPE ?? '';

export const isEntraAuth = authMode === 'entra';
