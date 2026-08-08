import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, errMsg } from '../api/client';
import { useAuth } from '../auth';
import type { WorkOrder } from '../types';
import { PRIORITY_LABEL } from '../types';
import { Loading, PageError, SlaBadge, StatusBadge, fmtDate } from '../components/ui';

export default function Portal() {
  const { sites, user } = useAuth();
  const navigate = useNavigate();
  const [orders, setOrders] = useState<WorkOrder[] | null>(null);
  const [error, setError] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [siteId, setSiteId] = useState('');
  const [priority, setPriority] = useState<'LOW' | 'MEDIUM' | 'HIGH'>('MEDIUM');
  const [busy, setBusy] = useState(false);

  const load = () => {
    api.get<{ content: WorkOrder[] }>('/work-orders?page=0&size=100').then((r) => setOrders(r.data.content)).catch((e) => setError(errMsg(e)));
  };
  useEffect(() => { load(); /* eslint-disable-next-line */ }, []);

  const raise = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      const res = await api.post<{ id: number }>('/work-orders', {
        title, description, priority,
        customerId: user?.customerId, siteId: Number(siteId),
      });
      setTitle(''); setDescription(''); setSiteId('');
      navigate(`/work-orders/${res.data.id}`);
    } catch (err) { setError(errMsg(err)); setBusy(false); }
  };

  return (
    <div className="page">
      <h2 className="page-title">Customer portal</h2>
      {error && <PageError message={error} />}

      <form className="card form" onSubmit={raise}>
        <h3 className="card-title">Raise a service request</h3>
        <label className="field"><span>Site *</span>
          <select value={siteId} onChange={(e) => setSiteId(e.target.value)} required>
            <option value="">Select your site…</option>
            {sites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </label>
        <label className="field"><span>Title *</span><input value={title} onChange={(e) => setTitle(e.target.value)} required /></label>
        <label className="field"><span>Description</span><textarea rows={4} value={description} onChange={(e) => setDescription(e.target.value)} /></label>
        <label className="field"><span>Priority</span>
          <select value={priority} onChange={(e) => setPriority(e.target.value as 'LOW' | 'MEDIUM' | 'HIGH')}>
            <option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option>
          </select>
        </label>
        <button className="btn btn-primary" disabled={busy}>{busy ? 'Submitting…' : 'Submit request'}</button>
      </form>

      <div className="card">
        <h3 className="card-title">My requests</h3>
        {!orders && <Loading />}
        {orders && orders.length === 0 && <p className="muted">You have no requests yet.</p>}
        {orders && orders.length > 0 && (
          <table className="table">
            <thead><tr><th>Code</th><th>Title</th><th>Priority</th><th>Status</th><th>SLA</th><th>Updated</th></tr></thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id} className="clickable" onClick={() => navigate(`/work-orders/${o.id}`)}>
                  <td className="mono">{o.code}</td>
                  <td>{o.title}</td>
                  <td><span className={`prio prio-${o.priority.toLowerCase()}`}>{PRIORITY_LABEL[o.priority]}</span></td>
                  <td><StatusBadge status={o.status} /></td>
                  <td><SlaBadge status={o.slaStatus} /></td>
                  <td className="muted">{fmtDate(o.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}