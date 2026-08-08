import { FormEvent, useEffect, useState } from 'react';
import { api, errMsg } from '../api/client';
import type { Customer, Paged, Role, User } from '../types';
import { EmptyState, PageError } from '../components/ui';

export default function Users() {
  const [data, setData] = useState<Paged<User> | null>(null);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [error, setError] = useState('');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<Role>('TECHNICIAN');
  const [password, setPassword] = useState('');
  const [customerId, setCustomerId] = useState('');

  const load = () => {
    api.get<Paged<User>>('/users?page=0&size=100').then((r) => setData(r.data)).catch((e) => setError(errMsg(e)));
  };
  useEffect(() => { load(); api.get<{ content: Customer[] }>('/customers?page=0&size=100').then((r) => setCustomers(r.data.content)).catch(() => {}); }, []);

  const create = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/users', { fullName, email, role, password, customerId: role === 'CUSTOMER' ? Number(customerId) : undefined });
      setFullName(''); setEmail(''); setPassword('');
      load();
    } catch (err) { setError(errMsg(err)); }
  };

  return (
    <div className="page">
      <h2 className="page-title">Users</h2>
      {error && <PageError message={error} />}

      <div className="card form">
        <h3 className="card-title">New user</h3>
        <div className="grid-2">
          <label className="field"><span>Full name *</span><input value={fullName} onChange={(e) => setFullName(e.target.value)} /></label>
          <label className="field"><span>Email *</span><input value={email} onChange={(e) => setEmail(e.target.value)} /></label>
          <label className="field"><span>Role *</span>
            <select value={role} onChange={(e) => setRole(e.target.value as Role)}>
              <option value="MANAGER">Manager</option><option value="DISPATCHER">Dispatcher</option>
              <option value="TECHNICIAN">Technician</option><option value="CUSTOMER">Customer</option>
            </select>
          </label>
          <label className="field"><span>Password *</span><input type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></label>
          {role === 'CUSTOMER' && (
            <label className="field"><span>Linked customer *</span>
              <select value={customerId} onChange={(e) => setCustomerId(e.target.value)}>
                <option value="">Select…</option>
                {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </label>
          )}
        </div>
        <button className="btn btn-primary" onClick={create}>Create user</button>
      </div>

      <div className="card">
        <h3 className="card-title">All users</h3>
        {data && data.content.length === 0 && <EmptyState title="No users" />}
        {data && data.content.length > 0 && (
          <table className="table">
            <thead><tr><th>Name</th><th>Email</th><th>Role</th></tr></thead>
            <tbody>{data.content.map((u) => <tr key={u.id}><td>{u.fullName}</td><td>{u.email}</td><td>{u.role}</td></tr>)}</tbody>
          </table>
        )}
      </div>
    </div>
  );
}