import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";
import PageHeader from "../components/PageHeader";
import { useAuth } from "../context/AuthContext";

function UsersPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    setLoading(true);
    setError("");

    try {
      const data = await api.getUsers();
      setUsers(data);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="User Management"
        title="Manage users, roles, and status"
        subtitle="This page is restricted to admins and keeps access control concerns separate from the records flow."
        action={<Link className="page-link-button" to="/app/users/new">Create User</Link>}
      />

      {error ? <div className="error-banner">{error}</div> : null}

      <section className="grid content-grid">
        <div className="panel large-panel">
          <h2>Users</h2>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((entry) => (
                <tr key={entry.id}>
                  <td>{entry.name}</td>
                  <td>{entry.email}</td>
                  <td>{entry.role}</td>
                  <td>{entry.status}</td>
                  <td className="actions">
                    <button className="ghost-button" type="button" onClick={() => navigate(`/app/users/${entry.id}/edit`)}>
                      Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="panel">
          <h2>Admin Actions</h2>
          <div className="list">
            <Link className="list-item selectable-item" to="/app/users/new">
              <strong>Create User Page</strong>
              <span>Dedicated admin page for onboarding users</span>
            </Link>
            <div className="list-item">
              <strong>Current Admin</strong>
              <span>{user?.email}</span>
              <span>{loading ? "Loading" : "Ready"}</span>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

export default UsersPage;
