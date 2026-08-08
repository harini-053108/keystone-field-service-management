import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, errMsg } from '../api/client';
import type { Customer, Paged } from '../types';
import { EmptyState, Loading, PageError, Pagination } from '../components/ui';

export default function Customers() {
  const [data, setData] = useState<Paged<Customer> | null>(null);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const navigate = useNavigate();

  const load = (p = page, q = search) => {
    api.get<Paged<Customer>>('/customers', { params: { page: p, size: 10, search: q || undefined } })
      .then((r) => setData(r.data)).catch((e) => setError(errMsg(e)));
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page]);

  const create = async () => {
    setError('');
    try {
      await api.post('/customers', { name, email, phone, address });
      setName(''); setEmail(''); setPhone(''); setAddress(''); setShowForm(false);
      setPage(0); load(0, search);
    } catch (e) { setError(errMsg(e)); }
  };

  return (
    <div className="page">
      <div className="page-head">
        <h2 className="page-title">Customers</h2>
        <button className="btn btn-primary" onClick={() => setShowForm((s) => !s)}>{showForm ? 'Close' : '+ New customer'}</button>
      </div>

      {showForm && (
        <div className="card form">
          <label className="field"><span>Name *</span><input value={name} onChange={(e) => setName(e.target.value)} /></label>
          <label className="field"><span>Email</span><input value={email} onChange={(e) => setEmail(e.target.value)} /></label>
          <label className="field"><span>Phone</span><input value={phone} onChange={(e) => setPhone(e.target.value)} /></label>
          <label className="field"><span>Address</span><textarea value={address} onChange={(e) => setAddress(e.target.value)} rows={2} /></label>
          <button className="btn btn-primary" onClick={create}>Create customer</button>
        </div>
      )}

      <div className="filters">
        <input className="input" placeholder="Search by name or email" value={search}
               onChange={(e) => setSearch(e.target.value)}
               onKeyDown={(e) => { if (e.key === 'Enter') { setPage(0); load(0, search); } }} />
        <button className="btn" onClick={() => { setPage(0); load(0, search); }}>Search</button>
      </div>

      {error && <PageError message={error} />}
      {!data && !error && <Loading />}
      {data && data.content.length === 0 && <EmptyState title="No customers" />}
      {data && data.content.length > 0 && (
        <table className="table">
          <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Sites</th></tr></thead>
          <tbody>
            {data.content.map((c) => (
              <tr key={c.id} className="clickable" onClick={() => navigate(`/customers/${c.id}`)}>
                <td>{c.name}</td><td>{c.email}</td><td>{c.phone}</td><td>{c.siteCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </div>
  );
}