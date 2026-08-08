import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, errMsg } from '../api/client';
import type { Customer, Priority, Site } from '../types';

export default function NewWorkOrder() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [sites, setSites] = useState<Site[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [customerId, setCustomerId] = useState('');
  const [siteId, setSiteId] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.get<{ content: Customer[] }>('/customers?page=0&size=100').then((r) => setCustomers(r.data.content)).catch((e) => setError(errMsg(e)));
  }, []);

  const loadSites = (cid: string) => {
    setCustomerId(cid); setSiteId('');
    if (cid) api.get<Site[]>(`/customers/${cid}/sites`).then((r) => setSites(r.data)).catch(() => setSites([]));
    else setSites([]);
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      const res = await api.post<{ id: number }>('/work-orders', { title, description, priority, customerId: Number(customerId), siteId: Number(siteId) });
      navigate(`/work-orders/${res.data.id}`);
    } catch (err) { setError(errMsg(err)); setBusy(false); }
  };

  return (
    <div className="page narrow">
      <h2 className="page-title">New work order</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <form className="card form" onSubmit={submit}>
        <label className="field"><span>Title *</span>
          <input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={255} />
        </label>
        <label className="field"><span>Description</span>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} />
        </label>
        <label className="field"><span>Priority *</span>
          <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
            <option value="LOW">Low</option><option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option><option value="URGENT">Urgent</option>
          </select>
        </label>
        <label className="field"><span>Customer *</span>
          <select value={customerId} onChange={(e) => loadSites(e.target.value)} required>
            <option value="">Select customer…</option>
            {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </label>
        <label className="field"><span>Site *</span>
          <select value={siteId} onChange={(e) => setSiteId(e.target.value)} required disabled={!customerId}>
            <option value="">Select site…</option>
            {sites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </label>
        <div className="btn-row">
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Creating…' : 'Create work order'}</button>
          <button type="button" className="btn" onClick={() => navigate(-1)}>Cancel</button>
        </div>
      </form>
    </div>
  );
}