import { FormEvent, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api, errMsg } from '../api/client';
import type { Customer, Site } from '../types';
import { EmptyState, Loading, PageError } from '../components/ui';

export default function CustomerDetail() {
  const { id } = useParams();
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [sites, setSites] = useState<Site[]>([]);
  const [error, setError] = useState('');
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');

  const load = () => {
    api.get<Customer>(`/customers/${id}`).then((r) => setCustomer(r.data)).catch((e) => setError(errMsg(e)));
    api.get<Site[]>(`/customers/${id}/sites`).then((r) => setSites(r.data)).catch(() => {});
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [id]);

  const addSite = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await api.post(`/customers/${id}/sites`, { name, address });
      setName(''); setAddress(''); load();
    } catch (err) { setError(errMsg(err)); }
  };

  if (error && !customer) return <PageError message={error} />;
  if (!customer) return <Loading />;

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <div className="mono page-eyebrow">Customer #{customer.id}</div>
          <h2 className="page-title">{customer.name}</h2>
        </div>
      </div>
      <p className="muted">{customer.address}</p>

      <div className="card form">
        <h3 className="card-title">Add site</h3>
        <label className="field"><span>Site name *</span><input value={name} onChange={(e) => setName(e.target.value)} /></label>
        <label className="field"><span>Address</span><input value={address} onChange={(e) => setAddress(e.target.value)} /></label>
        <button className="btn btn-primary" onClick={addSite}>Add site</button>
      </div>

      <div className="card">
        <h3 className="card-title">Sites</h3>
        {sites.length === 0 ? <EmptyState title="No sites yet" /> : (
          <table className="table">
            <thead><tr><th>Name</th><th>Address</th></tr></thead>
            <tbody>{sites.map((s) => <tr key={s.id}><td>{s.name}</td><td>{s.address}</td></tr>)}</tbody>
          </table>
        )}
      </div>
    </div>
  );
}