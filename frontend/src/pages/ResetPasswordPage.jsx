import { useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../api";

function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const initialToken = useMemo(() => searchParams.get("token") || "", [searchParams]);
  const [form, setForm] = useState({ token: initialToken, newPassword: "", confirmPassword: "" });
  const [result, setResult] = useState("");
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      const response = await api.resetPassword(form);
      setResult(response.message);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card panel">
        <p className="eyebrow">Reset Password</p>
        <h1>Choose a new password</h1>
        <p className="subtitle">Use the token from the forgot-password flow to complete the reset.</p>

        {error ? <div className="error-banner">{error}</div> : null}
        {result ? <div className="success-banner">{result}</div> : null}

        <form className="form-grid" onSubmit={handleSubmit}>
          <input
            placeholder="Reset token"
            value={form.token}
            onChange={(event) => setForm({ ...form, token: event.target.value })}
            required
          />
          <input
            type="password"
            placeholder="New password"
            value={form.newPassword}
            onChange={(event) => setForm({ ...form, newPassword: event.target.value })}
            required
          />
          <input
            type="password"
            placeholder="Confirm new password"
            value={form.confirmPassword}
            onChange={(event) => setForm({ ...form, confirmPassword: event.target.value })}
            required
          />
          <button type="submit">Reset Password</button>
        </form>

        <Link className="text-link" to="/login">
          Back to login
        </Link>
      </div>
    </div>
  );
}

export default ResetPasswordPage;
