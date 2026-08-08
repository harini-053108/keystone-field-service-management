import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';
import { errMsg } from '../api/client';

const DEMO = [
  { label: 'Manager', email: 'manager@example.com' },
  { label: 'Dispatcher', email: 'dispatcher@example.com' },
  { label: 'Technician', email: 'tech1@example.com' },
  { label: 'Customer', email: 'customer@example.com' },
];

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await login(email, password);
      navigate('/', { replace: true });
    } catch (err) {
      setError(errMsg(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-wrap">
      <div className="login-card">
        <div className="login-brand">
          <div className="brand-mark lg">K</div>
          <div className="login-title">KEYSTONE</div>
          <div className="login-sub">Field Service Management · Meridian Facilities</div>
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={submit} className="form">
          <label className="field">
            <span>Email</span>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="username" />
          </label>
          <label className="field">
            <span>Password</span>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required autoComplete="current-password" />
          </label>
          <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
        </form>
        <div className="demo">
          <div className="demo-label">Demo accounts (password: Keystone@123)</div>
          <div className="demo-row">
            {DEMO.map((d) => (
              <button key={d.email} className="chip" onClick={() => { setEmail(d.email); setPassword('Keystone@123'); }}>
                {d.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}