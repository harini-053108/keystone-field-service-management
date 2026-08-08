import { useEffect } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useAuth } from './auth';
import type { Role } from './types';
import Login from './pages/Login';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Board from './pages/Board';
import WorkOrders from './pages/WorkOrders';
import WorkOrderDetail from './pages/WorkOrderDetail';
import NewWorkOrder from './pages/NewWorkOrder';
import Customers from './pages/Customers';
import CustomerDetail from './pages/CustomerDetail';
import Users from './pages/Users';
import Parts from './pages/Parts';
import MyJobs from './pages/MyJobs';
import Portal from './pages/Portal';

function Protected({ roles, children }: { roles?: Role[]; children: JSX.Element }) {
  const { token, user, role, loadMe } = useAuth();
  const location = useLocation();

  useEffect(() => {
    if (token && !user) loadMe().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  if (!token) return <Navigate to="/login" replace />;
  if (!user || !role) {
    return <div className="center-page"><div className="spinner" /></div>;
  }
  if (roles && !roles.includes(role)) return <Navigate to="/" replace state={{ from: location }} />;
  return <Layout>{children}</Layout>;
}

export default function App() {
  const { user } = useAuth();

  const homePath = user
    ? ({ DISPATCHER: '/board', TECHNICIAN: '/my-jobs', MANAGER: '/dashboard', CUSTOMER: '/portal' } as Record<string, string>)[user.role] ?? '/'
    : '/login';

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Navigate to={homePath} replace />} />

      <Route path="/dashboard" element={<Protected roles={['MANAGER']}><Dashboard /></Protected>} />
      <Route path="/board" element={<Protected roles={['DISPATCHER', 'MANAGER']}><Board /></Protected>} />
      <Route path="/work-orders" element={<Protected roles={['DISPATCHER', 'MANAGER', 'TECHNICIAN']}><WorkOrders /></Protected>} />
      <Route path="/work-orders/new" element={<Protected roles={['DISPATCHER', 'MANAGER']}><NewWorkOrder /></Protected>} />
      <Route path="/work-orders/:id" element={<Protected><WorkOrderDetail /></Protected>} />
      <Route path="/customers" element={<Protected roles={['DISPATCHER', 'MANAGER']}><Customers /></Protected>} />
      <Route path="/customers/:id" element={<Protected roles={['DISPATCHER', 'MANAGER']}><CustomerDetail /></Protected>} />
      <Route path="/users" element={<Protected roles={['MANAGER']}><Users /></Protected>} />
      <Route path="/parts" element={<Protected roles={['MANAGER']}><Parts /></Protected>} />
      <Route path="/my-jobs" element={<Protected roles={['TECHNICIAN']}><MyJobs /></Protected>} />
      <Route path="/portal" element={<Protected roles={['CUSTOMER']}><Portal /></Protected>} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}