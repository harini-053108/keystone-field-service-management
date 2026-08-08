import { useEffect, useState } from 'react';
import { api, errMsg } from '../api/client';
import type { ReportSummary } from '../types';
import { Card, EmptyState, Loading, PageError, SlaBadge, StatusBadge } from '../components/ui';

const STATUS_ORDER = ['NEW', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CLOSED', 'CANCELLED'];

export default function Dashboard() {
  const [data, setData] = useState<ReportSummary | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get<ReportSummary>('/reports/summary').then((r) => setData(r.data)).catch((e) => setError(errMsg(e)));
  }, []);

  if (error) return <PageError message={error} />;
  if (!data) return <Loading />;

  return (
    <div className="page">
      <h2 className="page-title">Operations Dashboard</h2>
      <div className="stat-grid">
        <Stat label="Open work orders" value={data.totalOpen} tone="primary" />
        <Stat label="Overdue" value={data.overdue} tone="danger" />
        <Stat label="SLA breaches" value={data.breached} tone="danger" />
        <Stat label="SLA compliance" value={`${data.slaCompliancePercent.toFixed(1)}%`} tone="good" />
      </div>

      <div className="grid-2">
        <Card title="Counts by status">
          <div className="bar-list">
            {STATUS_ORDER.map((s) => {
              const count = data.countsByStatus[s] ?? 0;
              const max = Math.max(...Object.values(data.countsByStatus), 1);
              return (
                <div key={s} className="bar-row">
                  <span className="bar-label"><StatusBadge status={s as never} /></span>
                  <div className="bar-track"><div className="bar-fill" style={{ width: `${(count / max) * 100}%` }} /></div>
                  <span className="bar-value">{count}</span>
                </div>
              );
            })}
          </div>
        </Card>

        <Card title="SLA status (open)">
          <div className="bar">
            {(['ON_TRACK', 'AT_RISK', 'BREACHED'] as const).map((s) => {
              const count = data.countsBySla[s] ?? 0;
              const max = Math.max(...Object.values(data.countsBySla), 1);
              return (
                <div key={s} className="bar-row">
                  <span className="bar-label"><SlaBadge status={s} /></span>
                  <div className="bar-track"><div className="bar-fill" style={{ width: `${(count / max) * 100}%` }} /></div>
                  <span className="bar-value">{count}</span>
                </div>
              );
            })}
          </div>
        </Card>
      </div>

      <div className="grid-2">
        <Card title="By technician">
          {data.byTechnician.length === 0 ? <EmptyState title="No assignments yet" /> : (
            <table className="table">
              <tbody>
                {data.byTechnician.map((t) => (
                  <tr key={t.name}><td>{t.name}</td><td className="right muted">{t.count} jobs</td></tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
        <Card title="By site">
          {data.bySite.length === 0 ? <EmptyState title="No sites yet" /> : (
            <table className="table">
              <tbody>
                {data.bySite.map((t) => (
                  <tr key={t.name}><td>{t.name}</td><td className="right muted">{t.count} jobs</td></tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      </div>

      <Card title="Overdue work">
        <p className="muted">See the board and work-orders list for overdue items flagged by SLA status.</p>
      </Card>
    </div>
  );
}

function Stat({ label, value, tone }: { label: string; value: number | string; tone: string }) {
  return (
    <div className={`stat ${tone}`}>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}