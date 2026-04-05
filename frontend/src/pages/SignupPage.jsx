import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function SignupPage() {
  const [form, setForm] = useState({ name: "", email: "", password: "", confirmPassword: "" });
  const [error, setError] = useState("");
  const { signup } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      await signup(form);
      navigate("/app/dashboard", { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card panel">
        <p className="eyebrow">Create Account</p>
        <h1>Signup as a new viewer</h1>
        <p className="subtitle">
          Self-signup creates an active viewer account. Admins can later change role or status from
          the users page.
        </p>

        {error ? <div className="error-banner">{error}</div> : null}

        <form className="form-grid" onSubmit={handleSubmit}>
          <input
            placeholder="Full name"
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            required
          />
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
          <input
            type="password"
            placeholder="Confirm password"
            value={form.confirmPassword}
            onChange={(event) => setForm({ ...form, confirmPassword: event.target.value })}
            required
          />
          <button type="submit">Create Account</button>
        </form>

        <Link className="text-link" to="/login">
          Already have an account? Back to login
        </Link>
      </div>
    </div>
  );
}

export default SignupPage;
