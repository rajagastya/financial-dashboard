import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import PageHeader from "../components/PageHeader";
import { useAuth } from "../context/AuthContext";

const emptyUser = {
  name: "",
  email: "",
  role: "VIEWER",
  status: "ACTIVE",
  password: "",
};

function UserFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const isEdit = useMemo(() => Boolean(id), [id]);
  const [form, setForm] = useState(emptyUser);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isEdit) {
      loadUser();
    }
  }, [id]);

  async function loadUser() {
    setError("");
    try {
      const targetUser = await api.getUser(id);
      setForm({
        name: targetUser.name,
        email: targetUser.email,
        role: targetUser.role,
        status: targetUser.status,
        password: "",
      });
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      if (isEdit) {
        const updated = await api.updateUser(id, form);
        if (updated.id === user?.id) {
          updateUser(updated);
        }
      } else {
        await api.createUser(form);
      }
      navigate("/app/users");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="User Form"
        title={isEdit ? "Edit user account" : "Create user account"}
        subtitle="A dedicated page keeps admin user operations separate from the user listing."
      />

      {error ? <div className="error-banner">{error}</div> : null}

      <section className="panel form-panel">
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
          <select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value })}>
            <option value="VIEWER">Viewer</option>
            <option value="ANALYST">Analyst</option>
            <option value="ADMIN">Admin</option>
          </select>
          <select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value })}>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
          <input
            type="password"
            placeholder={isEdit ? "New password (optional)" : "Password"}
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            required={!isEdit}
          />
          <div className="button-row">
            <button type="button" className="ghost-button" onClick={() => navigate("/app/users")}>
              Cancel
            </button>
            <button type="submit">{isEdit ? "Update User" : "Create User"}</button>
          </div>
        </form>
      </section>
    </>
  );
}

export default UserFormPage;
