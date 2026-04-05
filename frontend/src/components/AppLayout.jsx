import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  return (
    <div className="app-shell">
      <aside className="sidebar panel">
        <div>
          <p className="eyebrow">Finance Backend</p>
          <h1 className="sidebar-title">Dashboard System</h1>
          <p className="helper">Separate screens for summaries, records, and user management.</p>
        </div>

        <nav className="nav-links">
          <NavLink to="/app/dashboard" className="nav-link">
            Dashboard
          </NavLink>
          <NavLink to="/app/analytics" className="nav-link">
            Analytics
          </NavLink>
          <NavLink to="/app/activity" className="nav-link">
            Activity
          </NavLink>
          <NavLink to="/app/records" className="nav-link">
            Records
          </NavLink>
          {user?.role === "ADMIN" ? (
            <NavLink to="/app/users" className="nav-link">
              Users
            </NavLink>
          ) : null}
          <NavLink to="/app/profile" className="nav-link">
            Profile
          </NavLink>
          <NavLink to="/app/security" className="nav-link">
            Security
          </NavLink>
        </nav>

        <div className="profile-card">
          <strong>{user?.name}</strong>
          <span>{user?.email}</span>
          <span className="pill">{user?.role}</span>
          <button type="button" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </aside>

      <main className="content-shell">
        <Outlet />
      </main>
    </div>
  );
}

export default AppLayout;
