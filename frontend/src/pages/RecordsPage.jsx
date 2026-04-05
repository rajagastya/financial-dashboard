import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";
import PageHeader from "../components/PageHeader";
import { useAuth } from "../context/AuthContext";
import { formatCurrency } from "../utils/format";

function RecordsPage() {
  const { user } = useAuth();
  const [filters, setFilters] = useState({ startDate: "", endDate: "", category: "", type: "" });
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const isAdmin = user?.role === "ADMIN";
  const navigate = useNavigate();

  useEffect(() => {
    loadRecords();
  }, []);

  async function loadRecords(activeFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const data = await api.getRecords(activeFilters);
      setRecords(data);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadRecords(filters);
  }

  async function handleDelete(recordId) {
    setError("");
    try {
      await api.deleteRecord(recordId);
      await loadRecords();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Financial Records"
        title="Browse and manage entries"
        subtitle="This page handles record filtering and CRUD operations separately from dashboard summaries."
        action={
          isAdmin ? <Link className="page-link-button" to="/app/records/new">Create Record</Link> : <span className="pill">{loading ? "Loading" : "Ready"}</span>
        }
      />

      {error ? <div className="error-banner">{error}</div> : null}

      <section className="grid content-grid">
        <div className="panel">
          <h2>Filters</h2>
          <form className="form-grid" onSubmit={handleFilterSubmit}>
            <input
              type="date"
              value={filters.startDate}
              onChange={(event) => setFilters({ ...filters, startDate: event.target.value })}
            />
            <input
              type="date"
              value={filters.endDate}
              onChange={(event) => setFilters({ ...filters, endDate: event.target.value })}
            />
            <input
              placeholder="Category"
              value={filters.category}
              onChange={(event) => setFilters({ ...filters, category: event.target.value })}
            />
            <select
              value={filters.type}
              onChange={(event) => setFilters({ ...filters, type: event.target.value })}
            >
              <option value="">All types</option>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
            <button type="submit">Apply Filters</button>
          </form>
        </div>

        <div className="panel">
          <h2>Access</h2>
          <p className="helper">
            Viewers and analysts can read records. Only admins can create, update, and delete.
          </p>
        </div>
      </section>

      <section className="grid content-grid">
        <div className="panel large-panel">
          <h2>Records List</h2>
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Category</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Notes</th>
                {isAdmin ? <th>Actions</th> : null}
              </tr>
            </thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.id}>
                  <td>{record.transactionDate}</td>
                  <td>{record.category}</td>
                  <td>{record.type}</td>
                  <td>{formatCurrency(record.amount)}</td>
                  <td>{record.notes || "-"}</td>
                  {isAdmin ? (
                    <td className="actions">
                      <button className="ghost-button" type="button" onClick={() => navigate(`/app/records/${record.id}/edit`)}>
                        Edit
                      </button>
                      <button className="danger-button" type="button" onClick={() => handleDelete(record.id)}>
                        Delete
                      </button>
                    </td>
                  ) : null}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="panel">
          <h2>Record Pages</h2>
          <div className="list">
            <Link className="list-item selectable-item" to="/app/analytics">
              <strong>Analytics Page</strong>
              <span>Use this for summary-only insights</span>
            </Link>
            {isAdmin ? (
              <Link className="list-item selectable-item" to="/app/records/new">
                <strong>Create Record Page</strong>
                <span>Dedicated page for creating entries</span>
              </Link>
            ) : null}
          </div>
        </div>
      </section>
    </>
  );
}

export default RecordsPage;
