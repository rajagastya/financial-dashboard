import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import PageHeader from "../components/PageHeader";
import { useAuth } from "../context/AuthContext";

function SecurityPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    try {
      const response = await api.changePassword(form);
      setSuccess(response.message);
      setForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  return (
    <>
      <PageHeader
        eyebrow="Security"
        title="Password and session security"
        subtitle="Change your password and manage your current authenticated session."
      />

      {error ? <div className="error-banner">{error}</div> : null}
      {success ? <div className="success-banner">{success}</div> : null}

      <section className="grid content-grid">
        <div className="panel form-panel">
          <h2>Change Password</h2>
          <form className="form-grid" onSubmit={handleSubmit}>
            <input
              type="password"
              placeholder="Current password"
              value={form.currentPassword}
              onChange={(event) => setForm({ ...form, currentPassword: event.target.value })}
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
            <button type="submit">Update Password</button>
          </form>
        </div>

        <div className="panel">
          <h2>Session Controls</h2>
          <p className="helper">
            Logging out clears the local JWT session and revokes the active refresh token.
          </p>
          <button type="button" className="danger-button" onClick={handleLogout}>
            Logout Current Session
          </button>
        </div>
      </section>
    </>
  );
}

export default SecurityPage;
