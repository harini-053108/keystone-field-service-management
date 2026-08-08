import type { ReactNode } from 'react';
import type { SlaStatus, WorkOrderStatus } from '../types';

export function Spinner() {
  return <div className="spinner" aria-label="loading" />;
}

export function Loading() {
  return (
    <div className="center-page">
      <Spinner />
    </div>
  );
}

export function EmptyState({ title, hint }: { title: string; hint?: string }) {
  return (
    <div className="empty">
      <div className="empty-icon">▤</div>
      <div className="empty-title">{title}</div>
      {hint && <div className="empty-hint">{hint}</div>}
    </div>
  );
}

export function PageError({ message }: { message: string }) {
  return (
    <div className="alert alert-error">
      <strong>Error:</strong> {message}
    </div>
  );
}

export const STATUS_LABEL: Record<WorkOrderStatus, string> = {
  NEW: 'New', ASSIGNED: 'Assigned', IN_PROGRESS: 'In progress', ON_HOLD: 'On hold',
  COMPLETED: 'Completed', CLOSED: 'Closed', CANCELLED: 'Cancelled',
};

export function StatusBadge({ status }: { status: WorkOrderStatus }) {
  return <span className={`badge badge-status-${status.toLowerCase()}`}>{STATUS_LABEL[status]}</span>;
}

export const SLA_LABEL: Record<SlaStatus, string> = {
  ON_TRACK: 'On track', AT_RISK: 'At risk', BREACHED: 'Breached', NOT_APPLICABLE: '—',
};

export function SlaBadge({ status }: { status: SlaStatus }) {
  if (status === 'NOT_APPLICABLE') return <span className="muted">—</span>;
  return <span className={`badge badge-sla-${status.toLowerCase()}`}>{SLA_LABEL[status]}</span>;
}

export function fmtDate(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
}

export function fmtMoney(n: number): string {
  return '$' + n.toFixed(2);
}

export function fmtHours(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h ${m}m`;
}

export function Pagination({
  page, totalPages, onChange,
}: { page: number; totalPages: number; onChange: (p: number) => void }) {
  if (totalPages <= 1) return null;
  return (
    <div className="pagination">
      <button className="btn btn-ghost" disabled={page === 0} onClick={() => onChange(page - 1)}>‹ Prev</button>
      <span className="muted">Page {page + 1} of {Math.max(totalPages, 1)}</span>
      <button className="btn btn-ghost" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>Next ›</button>
    </div>
  );
}

export function Card({ title, actions, children }: { title: string; actions?: ReactNode; children: ReactNode }) {
  return (
    <section className="card">
      <header className="card-head">
        <h3 className="card-title">{title}</h3>
        {actions && <div className="card-actions">{actions}</div>}
      </header>
      <div className="card-body">{children}</div>
    </section>
  );
}