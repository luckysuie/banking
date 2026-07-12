import type { ApiResponse, ErrorResponse, PageResponse } from './types';
import { apiBaseUrl } from '../config/env';

const AUTH_KEY = 'cloudbank_auth';
const BEARER_KEY = 'cloudbank_bearer';

export function getStoredAuth(): string | null {
  return sessionStorage.getItem(AUTH_KEY);
}

export function setStoredAuth(username: string, password: string): void {
  const token = btoa(`${username}:${password}`);
  sessionStorage.setItem(AUTH_KEY, token);
}

export function getBearerToken(): string | null {
  return sessionStorage.getItem(BEARER_KEY);
}

export function setBearerToken(token: string): void {
  sessionStorage.setItem(BEARER_KEY, token);
}

export function clearStoredAuth(): void {
  sessionStorage.removeItem(AUTH_KEY);
  sessionStorage.removeItem(BEARER_KEY);
}

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public errorCode?: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

async function parseError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as ErrorResponse;
    return new ApiError(body.message || 'Request failed', response.status, body.errorCode);
  } catch {
    return new ApiError(`Request failed (${response.status})`, response.status);
  }
}

function buildUrl(path: string): string {
  const base = apiBaseUrl.endsWith('/') ? apiBaseUrl.slice(0, -1) : apiBaseUrl;
  return `${base}${path}`;
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const basicAuth = getStoredAuth();
  const bearer = getBearerToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };
  if (bearer) {
    headers.Authorization = `Bearer ${bearer}`;
  } else if (basicAuth) {
    headers.Authorization = `Basic ${basicAuth}`;
  }

  const response = await fetch(buildUrl(path), { ...options, headers });

  if (!response.ok) {
    throw await parseError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const body = (await response.json()) as ApiResponse<T>;
  return body.data;
}

export async function apiPageRequest<T>(
  path: string,
): Promise<PageResponse<T>> {
  return apiRequest<PageResponse<T>>(path);
}
