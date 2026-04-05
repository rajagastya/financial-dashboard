import { useEffect, useState } from "react";
import { api } from "../api";
import PageHeader from "../components/PageHeader";
import { useAuth } from "../context/AuthContext";

function ProfilePage() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({ name: "", email: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    if (user) {
      setForm({ name: user.name, email: user.email });
    }
  }, [user]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    try {
      const updatedUser = await api.updateProfile(form);
      updateUser(updatedUser);
      setSuccess("Profile updated successfully.");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Profile"
        title="Manage your profile"
        subtitle="Update your basic account information on a dedicated profile page."
      />

      {error ? <div className="error-banner">{error}</div> : null}
      {success ? <div className="success-banner">{success}</div> : null}

      <section className="grid content-grid">
        <div className="panel form-panel">
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
            <button type="submit">Save Profile</button>
          </form>
        </div>

        <div className="panel">
          <h2>Account Details</h2>
          <div className="list">
            <div className="list-item">
              <strong>Role</strong>
              <span>{user?.role}</span>
            </div>
            <div className="list-item">
              <strong>Status</strong>
              <span>{user?.status}</span>
            </div>
            <div className="list-item">
              <strong>Last Login</strong>
              <span>{user?.lastLoginAt || "First session"}</span>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

export default ProfilePage;
