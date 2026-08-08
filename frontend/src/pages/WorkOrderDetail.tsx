import { FormEvent, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api, errMsg } from '../api/client';
import { useAuth } from '../auth';
import type { Part, Role, User, WorkOrder, WorkOrderStatus } from '../types';
import { Card, EmptyState, Loading, PageError, SlaBadge, StatusBadge, fmtDate, fmtHours, fmtMoney } from '../components/ui';

interface Action { to: WorkOrderStatus; label: string; primary?: boolean }

function buildActions(status: WorkOrderStatus, role: Role, isAssignedTech: boolean): Action[] {
  const canDispatch = role === 'MANAGER' || role === 'DISPATCHER';
  switch (status) {
    case 'NEW':
      return canDispatch ? [{ to: 'CANCELLED', label: 'Cancel' }] : [];
    case 'ASSIGNED':
      return [
        ...(isAssignedTech ? [{ to: 'IN_PROGRESS' as const, label: 'Start work', primary: true }] : []),
        ...(canDispatch ? [{ to: 'CANCELLED' as const, label: 'Cancel' }] : []),
      ];
    case 'IN_PROGRESS':
      return isAssignedTech
        ? [{ to: 'ON_HOLD', label: 'Put on hold' }, { to: 'COMPLETED', label: 'Complete job', primary: true }]
        : [];
    case 'ON_HOLD':
      return isAssignedTech ? [{ to: 'IN_PROGRESS', label: 'Resume', primary: true }] : [];
    case 'COMPLETED':
      return role === 'MANAGER' ? [{ to: 'CLOSED', label: 'Close (sign off)', primary: true }] : [];
    default:
      return [];
  }
}

export default function WorkOrderDetail() {
  const { id } = useParams();
  const { user, role } = useAuth();
  const [wo, setWo] = useState<WorkOrder | null>(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const [techs, setTechs] = useState<User[]>([]);
  const [parts, setParts] = useState<Part[]>([]);
  const [assignTech, setAssignTech] = useState('');
  const [partId, setPartId] = useState('');
  const [qty, setQty] = useState(1);
  const [partNote, setPartNote] = useState('');
  const [minutes, setMinutes] = useState(30);

  const load = () => {
    api.get<WorkOrder>(`/work-orders/${id}`).then((r) => setWo(r.data)).catch((e) => setError(errMsg(e)));
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [id]);
  useEffect(() => {
    if (role === 'MANAGER' || role === 'DISPATCHER') {
      api.get<User[]>('/users/technicians').then((r) => setTechs(r.data)).catch(() => {});
    }
    api.get<{ content: Part[] }>('/parts?page=0&size=100')
      .then((r) => setParts(r.data.content)).catch(() => {});
  }, [role]);

  const post = async (path: string, body?: unknown) => {
    setError(''); setMessage('');
    try {
      await api.post(path, body ?? {});
      setMessage('Updated.');
      load();
    } catch (e) { setError(errMsg(e)); }
  };

  const doTransition = (status: WorkOrderStatus) => {
    const note = prompt('Note (optional):') ?? '';
    post(`/work-orders/${id}/status`, { status, note });
  };

  const doAssign = (e: FormEvent) => {
    e.preventDefault();
    if (assignTech) post(`/work-orders/${id}/assign`, { technicianId: Number(assignTech) });
  };

  const doLogPart = (e: FormEvent) => {
    e.preventDefault();
    if (partId && qty > 0) post(`/work-orders/${id}/parts`, { partId: Number(partId), quantity: qty, note: partNote });
  };

  const doLogTime = (e: FormEvent) => {
    e.preventDefault();
    if (minutes > 0) post(`/work-orders/${id}/time`, { minutes, note: partNote });
  };

  if (error && !wo) return <PageError message={error} />;
  if (!wo) return <Loading />;

  const isAssignedTech = role === 'TECHNICIAN' && wo.assigneeId === user?.id;
  const canDispatch = role === 'MANAGER' || role === 'DISPATCHER';
  const canLog = isAssignedTech || canDispatch;
  const actions = buildActions(wo.status, role ?? 'CUSTOMER', isAssignedTech);
  const isCustomer = role === 'CUSTOMER';

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <div className="mono page-eyebrow">{wo.code}</div>
          <h2 className="page-title">{wo.title}</h2>
        </div>
        <StatusBadge status={wo.status} />
      </div>

      {message && <div className="alert alert-success">{message}</div>}
      {error && <PageError message={error} />}

      <div className="grid-2">
        <Card title="Details">
          <dl className="details">
            <dt>Customer</dt><dd>{wo.customerName}</dd>
            <dt>Site</dt><dd>{wo.siteName}</dd>
            <dt>Priority</dt><dd>{wo.priority}</dd>
            <dt>SLA due</dt><dd>{fmtDate(wo.slaDueAt)} <SlaBadge status={wo.slaStatus} /></dd>
            <dt>Assignee</dt><dd>{wo.assigneeName ?? 'Unassigned'}</dd>
            <dt>Created</dt><dd>{fmtDate(wo.createdAt)}</dd>
            {wo.completedAt && <><dt>Completed</dt><dd>{fmtDate(wo.completedAt)}</dd></>}
          </dl>
          {wo.description && <div className="desc">{wo.description}</div>}
        </Card>

        <Card title="Actions">
          {actions.length === 0 && <p className="muted">No actions available for your role.</p>}
          <div className="btn-row">
            {actions.map((a) => (
              <button key={a.to} className={`btn ${a.primary ? 'btn-primary' : ''}`} onClick={() => doTransition(a.to)}>
                {a.label}
              </button>
            ))}
          </div>

          {canDispatch && wo.assigneeId == null && (
            <form className="form" onSubmit={doAssign}>
              <label className="field"><span>Assign technician</span>
                <select value={assignTech} onChange={(e) => setAssignTech(e.target.value)} required>
                  <option value="">Select…</option>
                  {techs.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
                </select>
              </label>
              <button className="btn btn-primary">Assign</button>
            </form>
          )}
          {canDispatch && wo.assigneeId != null && (
            <form className="form" onSubmit={doAssign}>
              <label className="field"><span>Reassign technician</span>
                <select value={assignTech} onChange={(e) => setAssignTech(e.target.value)}>
                  <option value="">{wo.assigneeName}</option>
                  {techs.filter((t) => t.id !== wo.assigneeId).map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
                </select>
              </label>
              <button className="btn">Reassign</button>
            </form>
          )}
        </Card>
      </div>

      {!isCustomer && (
        <div className="grid-2">
          <Card title="Log parts">
            <form className="form" onSubmit={doLogPart}>
              <label className="field"><span>Part</span>
                <select value={partId} onChange={(e) => setPartId(e.target.value)} required>
                  <option value="">Select…</option>
                  {parts.map((p) => <option key={p.id} value={p.id}>{p.name} (stock {p.stockQty})</option>)}
                </select>
              </label>
              <label className="field"><span>Quantity</span>
                <input type="number" min={1} value={qty} onChange={(e) => setQty(Number(e.target.value))} />
              </label>
              <label className="field"><span>Note</span><input value={partNote} onChange={(e) => setPartNote(e.target.value)} /></label>
              <button className="btn btn-primary" disabled={!canLog}>Log part</button>
            </form>
          </Card>
          <Card title="Log time">
            <form className="form" onSubmit={doLogTime}>
              <label className="field"><span>Minutes</span>
                <input type="number" min={1} value={minutes} onChange={(e) => setMinutes(Number(e.target.value))} />
              </label>
              <label className="field"><span>Note</span><input value={partNote} onChange={(e) => setPartNote(e.target.value)} /></label>
              <button className="btn btn-primary" disabled={!canLog}>Log time</button>
            </form>
          </Card>
        </div>
      )}

      {!isCustomer && (
        <div className="grid-2">
          <Card title={`Parts used — total ${fmtMoney(wo.totalPartsCost)}`}>
            {wo.partUsages.length === 0 ? <EmptyState title="No parts logged" /> : (
              <table className="table"><thead><tr><th>Part</th><th>Qty</th><th>Unit</th><th>Total</th></tr></thead>
                <tbody>{wo.partUsages.map((p) => (
                  <tr key={p.id}><td>{p.partName} <span className="mono muted">{p.partCode}</span></td>
                    <td>{p.quantity}</td><td>{fmtMoney(p.unitCost)}</td><td>{fmtMoney(p.totalCost)}</td></tr>))}
                </tbody>
              </table>
            )}
          </Card>
          <Card title={`Time — ${fmtHours(wo.totalMinutes)}`}>
            {wo.timeLogs.length === 0 ? <EmptyState title="No time logged" /> : (
              <table className="table"><thead><tr><th>Minutes</th><th>Note</th><th>By</th><th>When</th></tr></thead>
                <tbody>{wo.timeLogs.map((t) => (
                  <tr key={t.id}><td>{t.minutes}</td><td>{t.note}</td><td>{t.loggedByName}</td><td className="muted">{fmtDate(t.loggedAt)}</td></tr>))}
                </tbody>
              </table>
            )}
          </Card>
        </div>
      )}

      <Card title="Status history">
        {wo.history.length === 0 ? <EmptyState title="No history yet" /> : (
          <table className="table">
            <thead><tr><th>From</th><th>To</th><th>By</th><th>Note</th><th>When</th></tr></thead>
            <tbody>
              {wo.history.map((h) => (
                <tr key={h.id}>
                  <td>{h.fromStatus ?? '—'}</td>
                  <td><StatusBadge status={h.toStatus} /></td>
                  <td>{h.changedByName}</td>
                  <td>{h.note}</td>
                  <td className="muted">{fmtDate(h.changedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}