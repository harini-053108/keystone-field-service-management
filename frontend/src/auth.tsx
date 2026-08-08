import { createContext, useContext, useMemo, useState, ReactNode } from 'react';
import { api, getToken, setToken } from './api/client';
import type { AuthResponse, Role, Site, User } from './types';

interface Me {
  user: User;
  sites: Site[];
}

interface AuthContextValue {
  token: string | null;
  user: User | null;
  sites: Site[];
  role: Role | null;
  login: (email: string, password: string) => Promise<void>;
  loadMe: () => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(getToken());
  const [user, setUser] = useState<User | null>(null);
  const [sites, setSites] = useState<Site[]>([]);

  const login = async (email: string, password: string) => {
    const res = await api.post<AuthResponse>('/auth/login', { email, password });
    setToken(res.data.token);
    setTokenState(res.data.token);
    await loadMe();
  };

  const loadMe = async () => {
    const res = await api.get<Me>('/auth/me');
    setUser(res.data.user);
    setSites(res.data.sites || []);
  };

  const logout = () => {
    setToken(null);
    setTokenState(null);
    setUser(null);
    setSites([]);
  };

  const value = useMemo<AuthContextValue>(
    () => ({ token, user, sites, role: user?.role ?? null, login, loadMe, logout }),
    [token, user, sites]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}