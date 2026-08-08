import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, errMsg } from '../api/client';
import type { WorkOrder, WorkOrderStatus } from '../types';
import { PRIORITY_LABEL } from '../types';
import { Loading, PageError, SlaBadge, StatusBadge, fmtDate } from '../components/ui';

const COLUMNS: WorkOrderStatus[] = ['NEW', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED'];

export default function Board() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    api.get<WorkOrder[]>('/work-orders/board').then((r) => setOrders(r.data)).catch((e) => setError(errMsg(e)));
  }, []);

  if (error) return <PageError message={error} />;
  if (!orders) return <Loading />;

  return (
    <div className="page">
      <div className="page-head">
        <h2 className="page-title">Dispatch Board</h2>
        <button className="btn btn-primary" onClick={() => navigate('/work-orders/new')}>+ New work order</button>
      </div>
      <div className="board">
        {COLUMNS.map((status) => {
          const items = orders.filter((o) => o.status === status);
          return (
            <div key={status} className="board-col">
              <div className="board-col-head">
                <StatusBadge status={status} />
                <span className="muted">{items.length}</span>
              </div>
              <div className="board-col-body">
                {items.length === 0 && <div className="board-empty">—</div>}
                {items.map((o) => (
                  <div key={o.id} className="board-card" onClick={() => navigate(`/work-orders/${o.id}`)}>
                    <div className="board-card-top">
                      <span className="mono">{o.code}</span>
                      <span className={`prio prio-${o.priority.toLowerCase()}`}>{PRIORITY_LABEL[o.priority]}</span>
                    </div>
                    <div className="board-card-title">{o.title}</div>
                    <div className="muted small">{o.siteName}</div>
                    <div className="board-card-meta">
                      <SlaBadge status={o.slaStatus} />
                      <span className="muted small">{o.assigneeName ?? 'Unassigned'}</span>
                    </div>
                    {o.slaDueAt && <div className="muted small">Due {fmtDate(o.slaDueAt)}</div>}
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}