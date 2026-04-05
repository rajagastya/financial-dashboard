import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";
import MetricCard from "../components/MetricCard";
import PageHeader from "../components/PageHeader";

function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSummary();
  }, []);

  async function loadSummary() {
    setLoading(true);
    setError("");

    try {
      const data = await api.getSummary();
      setSummary(data);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Dashboard Summary"
        title="Overview of your finance activity"
        subtitle="High-level analytics, category totals, trends, and recent activity for the dashboard."
        action={<button onClick={loadSummary}>Refresh Summary</button>}
      />

      {error ? <div className="error-banner">{error}</div> : null}

      <section className="grid stats-grid">
        <MetricCard title="Total Income" value={summary?.totalIncome} />
        <MetricCard title="Total Expenses" value={summary?.totalExpenses} />
        <MetricCard title="Net Balance" value={summary?.netBalance} />
      </section>

      <section className="grid content-grid">
        <div className="panel">
          <h2>Quick Navigation</h2>
          <div className="list">
            <Link className="list-item selectable-item" to="/app/analytics">
              <strong>Analytics</strong>
              <span>Category totals and monthly trends</span>
            </Link>
            <Link className="list-item selectable-item" to="/app/activity">
              <strong>Activity</strong>
              <span>Recent finance activity and movement</span>
            </Link>
            <Link className="list-item selectable-item" to="/app/records">
              <strong>Records</strong>
              <span>Browse, filter, and manage transactions</span>
            </Link>
          </div>
        </div>

        <div className="panel">
          <h2>System Status</h2>
          <div className="list">
            <div className="list-item">
              <strong>Authentication</strong>
              <span>JWT access tokens with refresh-token rotation</span>
            </div>
            <div className="list-item">
              <strong>Authorization</strong>
              <span>{loading ? "Checking..." : "Role-based access control active"}</span>
            </div>
            <div className="list-item">
              <strong>Persistence</strong>
              <span>SQLite storage with seeded finance data</span>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

export default DashboardPage;
