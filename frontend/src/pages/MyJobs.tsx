import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, errMsg } from '../api/client';
import type { WorkOrder } from '../types';
import { PRIORITY_LABEL } from '../types';
import { EmptyState, Loading, PageError, SlaBadge, StatusBadge, fmtDate } from '../components/ui';

export default function MyJobs() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    api.get<{ content: WorkOrder[] }>('/work-orders?page=0&size=100').then((r) => setOrders(r.data.content)).catch((e) => setError(errMsg(e)));
  }, []);

  if (error) return <PageError message={error} />;
  if (!orders) return <Loading />;

  const closedOrCancelled = (s: string) => s === 'CLOSED' || s === 'CANCELLED';
  const active = orders.filter((o) => !closedOrCancelled(o.status) && o.status !== 'COMPLETED');
  const completed = orders.filter((o) => o.status === 'COMPLETED' || o.status === 'CLOSED');

  return (
    <div className="page">
      <h2 className="page-title">My jobs</h2>
      {orders.length === 0 && <EmptyState title="You have no assigned jobs" hint="Jobs assigned to you by the dispatcher appear here." />}

      {active.length > 0 && (
        <div className="card">
          <h3 className="card-title">Active</h3>
          <div className="job-list">
            {active.map((o) => (
              <div key={o.id} className="job-card" onClick={() => navigate(`/work-orders/${o.id}`)}>
                <div className="board-card-top">
                  <span className="mono">{o.code}</span>
                  <span className={`prio prio-${o.priority.toLowerCase()}`}>{PRIORITY_LABEL[o.priority]}</span>
                </div>
                <div className="job-title">{o.title}</div>
                <div className="muted small">{o.siteName}</div>
                <div className="board-card-meta">
                  <StatusBadge status={o.status} />
                  <SlaBadge status={o.slaStatus} />
                </div>
                <div className="muted small">Due {fmtDate(o.slaDueAt)}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {completed.length > 0 && (
        <div className="card">
          <h3 className="card-title">Completed</h3>
          <div className="job-list">
            {completed.map((j) => (
              <div key={j.id} className="job-card" onClick={() => navigate(`/work-orders/${j.id}`)}>
                <div className="mono">{j.code}</div>
                <div className="job-title">{j.title}</div>
                <div className="muted small">{j.siteName}</div>
                <StatusBadge status={j.status} />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}