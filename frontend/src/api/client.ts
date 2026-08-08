import axios, { AxiosError } from 'axios';
import type { ErrorResponse } from '../types';

const TOKEN_KEY = 'keystone_token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

export const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response?.status === 401) {
      setToken(null);
      if (!location.pathname.startsWith('/login')) location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/** Extract a human-readable message from an error response. */
export function errMsg(e: unknown): string {
  const err = e as AxiosError<ErrorResponse>;
  const data = err.response?.data;
  if (!data) return 'Something went wrong. Please try again.';
  const fields = data.fieldErrors ? Object.values(data.fieldErrors).join('; ') : null;
  return fields || data.message || data.error || 'Request failed';
}