import { FormEvent, useEffect, useState } from 'react';
import { api, errMsg } from '../api/client';
import type { Paged, Part } from '../types';
import { EmptyState, PageError } from '../components/ui';

export default function Parts() {
  const [data, setData] = useState<Paged<Part> | null>(null);
  const [error, setError] = useState('');
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [unitCost, setUnitCost] = useState(0);
  const [stockQty, setStockQty] = useState(0);

  const load = () => {
    api.get<Paged<Part>>('/parts?page=0&size=100').then((r) => setData(r.data)).catch((e) => setError(errMsg(e)));
  };
  useEffect(load, []);

  const create = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/parts', { code, name, unitCost, stockQty });
      setCode(''); setName(''); setUnitCost(0); setStockQty(0);
      load();
    } catch (err) { setError(errMsg(err)); }
  };

  return (
    <div className="page">
      <h2 className="page-title">Parts inventory</h2>
      {error && <PageError message={error} />}

      <div className="card form">
        <h3 className="card-title">New part</h3>
        <div className="grid-2">
          <label className="field"><span>Code *</span><input value={code} onChange={(e) => setCode(e.target.value)} /></label>
          <label className="field"><span>Name *</span><input value={name} onChange={(e) => setName(e.target.value)} /></label>
          <label className="field"><span>Unit cost *</span><input type="number" min={0} step="0.01" value={unitCost} onChange={(e) => setUnitCost(Number(e.target.value))} /></label>
          <label className="field"><span>Stock qty *</span><input type="number" min={0} value={stockQty} onChange={(e) => setStockQty(Number(e.target.value))} /></label>
        </div>
        <button className="btn btn-primary" onClick={create}>Create part</button>
      </div>

      <div className="card">
        <h3 className="card-title">Inventory</h3>
        {data && data.content.length === 0 && <EmptyState title="No parts" />}
        {data && data.content.length > 0 && (
          <table className="table">
            <thead><tr><th>Code</th><th>Name</th><th>Unit cost</th><th>Stock</th></tr></thead>
            <tbody>{data.content.map((p) => (
              <tr key={p.id}><td className="mono">{p.code}</td><td>{p.name}</td><td>${p.unitCost.toFixed(2)}</td>
                <td className={p.stockQty === 0 ? 'danger' : ''}>{p.stockQty}</td></tr>))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}