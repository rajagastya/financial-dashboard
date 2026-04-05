import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function LoginPage() {
  const [form, setForm] = useState({ email: "admin@finance.local", password: "Admin@123" });
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const redirectTo = location.state?.from?.pathname || "/app/dashboard";

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      await login(form);
      navigate(redirectTo, { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card panel">
        <p className="eyebrow">Welcome Back</p>
        <h1>Login to the finance dashboard</h1>
        <p className="subtitle">
          JWT authentication is now enabled. Use the seeded enterprise demo accounts below to sign
          in quickly.
        </p>

        {error ? <div className="error-banner">{error}</div> : null}

        <form className="form-grid" onSubmit={handleSubmit}>
          <input
            type="email"
            placeholder="Email address"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            required
          />
          <button type="submit">Login</button>
        </form>

        <div className="auth-actions">
          <Link className="text-link" to="/forgot-password">
            Forgot password?
          </Link>
          <Link className="text-link" to="/signup">
            Need an account? Create one
          </Link>
        </div>

        <div className="list compact-list">
          <div className="list-item">
            <strong>Admin</strong>
            <span>admin@finance.local</span>
            <span>Password: Admin@123</span>
          </div>
          <div className="list-item">
            <strong>Analyst</strong>
            <span>analyst@finance.local</span>
            <span>Password: Analyst@123</span>
          </div>
          <div className="list-item">
            <strong>Viewer</strong>
            <span>viewer@finance.local</span>
            <span>Password: Viewer@123</span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
