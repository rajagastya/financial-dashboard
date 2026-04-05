import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";

function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      const response = await api.forgotPassword(email);
      setResult(response);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card panel">
        <p className="eyebrow">Password Recovery</p>
        <h1>Forgot your password?</h1>
        <p className="subtitle">
          Enter your email to generate a password reset token for local development.
        </p>

        {error ? <div className="error-banner">{error}</div> : null}

        <form className="form-grid" onSubmit={handleSubmit}>
          <input
            type="email"
            placeholder="Email address"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
          <button type="submit">Generate Reset Token</button>
        </form>

        {result ? (
          <div className="panel nested-panel">
            <strong>{result.message}</strong>
            <p className="helper">Development reset token: {result.debugToken || "Unavailable"}</p>
            {result.debugToken ? (
              <button onClick={() => navigate(`/reset-password?token=${result.debugToken}`)} type="button">
                Continue to Reset Password
              </button>
            ) : null}
          </div>
        ) : null}

        <Link className="text-link" to="/login">
          Back to login
        </Link>
      </div>
    </div>
  );
}

export default ForgotPasswordPage;
