import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, errMsg } from '../api/client';
import type { Paged, WorkOrder } from '../types';
import { PRIORITY_LABEL } from '../types';
import { EmptyState, Loading, PageError, Pagination, SlaBadge, StatusBadge, fmtDate } from '../components/ui';

export default function WorkOrders() {
  const [data, setData] = useState<Paged<WorkOrder> | null>(null);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const navigate = useNavigate();

  const load = (p = page, q = search, s = status) => {
    api.get<Paged<WorkOrder>>('/work-orders', {
      params: { page: p, size: 10, sort: 'createdAt,desc', search: q || undefined, status: s || undefined },
    }).then((r) => setData(r.data)).catch((e) => setError(errMsg(e)));
  };

  useEffect(() => { load(page, search, status); /* eslint-disable-next-line */ }, [page]);

  const apply = () => { setPage(0); load(0, search, status); };

  return (
    <div className="page">
      <div className="page-head">
        <h2 className="page-title">Work Orders</h2>
        <button className="btn btn-primary" onClick={() => navigate('/work-orders/new')}>+ New work order</button>
      </div>

      <div className="filters">
        <input className="input" placeholder="Search by title or code" value={search}
               onChange={(e) => setSearch(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && apply()} />
        <select className="input" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); load(0, search, e.target.value); }}>
          <option value="">All statuses</option>
          <option>NEW</option><option>ASSIGNED</option><option>IN_PROGRESS</option>
          <option>ON_HOLD</option><option>COMPLETED</option><option>CLOSED</option><option>CANCELLED</option>
        </select>
        <button className="btn" onClick={apply}>Apply</button>
      </div>

      {error && <PageError message={error} />}
      {!data && !error && <Loading />}
      {data && data.content.length === 0 && <EmptyState title="No work orders" hint="Try adjusting the filters." />}
      {data && data.content.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>Code</th><th>Title</th><th>Priority</th><th>Status</th><th>SLA</th>
              <th>Site</th><th>Assignee</th><th>Due</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((o) => (
              <tr key={o.id} className="clickable" onClick={() => navigate(`/work-orders/${o.id}`)}>
                <td className="mono">{o.code}</td>
                <td>{o.title}</td>
                <td><span className={`prio prio-${o.priority.toLowerCase()}`}>{PRIORITY_LABEL[o.priority]}</span></td>
                <td><StatusBadge status={o.status} /></td>
                <td><SlaBadge status={o.slaStatus} /></td>
                <td className="muted">{o.siteName}</td>
                <td className="muted">{o.assigneeName ?? '—'}</td>
                <td className="muted">{fmtDate(o.slaDueAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </div>
  );
}