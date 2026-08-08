import { ReactNode, useEffect, useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';
import { api } from '../api/client';
import type { Notification } from '../types';

const NAV: Record<string, { to: string; label: string; icon: string }[]> = {
  MANAGER: [
    { to: '/dashboard', label: 'Dashboard', icon: '▦' },
    { to: '/board', label: 'Board', icon: '▤' },
    { to: '/work-orders', label: 'Work orders', icon: '▤' },
    { to: '/customers', label: 'Customers', icon: '▣' },
    { to: '/parts', label: 'Parts', icon: '◈' },
    { to: '/users', label: 'Users', icon: '▨' },
  ],
  DISPATCHER: [
    { to: '/board', label: 'Board', icon: '▤' },
    { to: '/work-orders', label: 'Work orders', icon: '▤' },
    { to: '/customers', label: 'Customers', icon: '▣' },
  ],
  TECHNICIAN: [
    { to: '/my-jobs', label: 'My jobs', icon: '▸' },
    { to: '/work-orders', label: 'All my jobs', icon: '▤' },
  ],
  CUSTOMER: [{ to: '/portal', label: 'My requests', icon: '▣' }],
};

export default function Layout({ children }: { children: ReactNode }) {
  const { user, role, token, logout } = useAuth();
  const navigate = useNavigate();
  const [unread, setUnread] = useState(0);
  const [notifs, setNotifs] = useState<Notification[]>([]);
  const [openNotifs, setOpenNotifs] = useState(false);

  const refreshNotifs = () => {
    if (!token) return;
    api.get<{ content: Notification[] }>('/notifications?page=0&size=5').then((r) => setNotifs(r.data.content)).catch(() => {});
    api.get<number>('/notifications/unread-count').then((r) => setUnread(r.data)).catch(() => {});
  };

  useEffect(() => {
    refreshNotifs();
    const id = setInterval(refreshNotifs, 30000);
    return () => clearInterval(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const markAll = async () => {
    await api.post('/notifications/read-all');
    refreshNotifs();
  };

  const goTo = (id: number | null) => {
    setOpenNotifs(false);
    if (id != null) navigate(`/work-orders/${id}`);
  };

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">K</div>
          <div>
            <div className="brand-name">KEYSTONE</div>
            <div className="brand-sub">Meridian Facilities</div>
          </div>
        </div>
        <nav className="nav">
          {(NAV[role ?? ''] ?? []).map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => 'nav-item' + (isActive ? ' active' : '')}>
              <span className="nav-icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-foot">
          {user && (
            <div className="me">
              <div className="me-name">{user.fullName}</div>
              <div className="me-role">{user.role}</div>
            </div>
          )}
          <button className="btn btn-ghost btn-block" onClick={() => { logout(); navigate('/login'); }}>
            Sign out
          </button>
        </div>
      </aside>

      <div className="main">
        <header className="topbar">
          <div className="topbar-title">Meridian Facilities Management</div>
          <div className="notif-wrap">
            <button className="notif-bell" onClick={() => { if (!openNotifs) refreshNotifs(); setOpenNotifs((o) => !o); }}>
              <span className="bell-icon">🔔</span>
              {unread > 0 && <span className="notif-dot">{unread}</span>}
            </button>
            {openNotifs && (
              <div className="notif-panel">
                <div className="notif-panel-head">
                  <span>Notifications</span>
                  <button className="btn btn-ghost btn-sm" onClick={markAll}>Mark all read</button>
                </div>
                {notifs.length === 0 && <div className="muted pad">No notifications</div>}
                {notifs.map((n) => (
                  <button key={n.id} className={'notif-item' + (n.read ? '' : ' unread')} onClick={() => goTo(n.workOrderId)}>
                    <div>{n.message}</div>
                    <div className="muted small">{new Date(n.createdAt).toLocaleString()}</div>
                  </button>
                ))}
              </div>
            )}
          </div>
        </header>

        <main className="content">{children}</main>
      </div>
    </div>
  );
}