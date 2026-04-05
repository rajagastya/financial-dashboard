import { useEffect, useState } from "react";
import { api } from "../api";
import PageHeader from "../components/PageHeader";
import { formatCurrency } from "../utils/format";

function ActivityPage() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    loadSummary();
  }, []);

  async function loadSummary() {
    setError("");
    try {
      setSummary(await api.getSummary());
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Recent Activity"
        title="Latest financial movement"
        subtitle="A separate activity feed page helps keep dashboard overview and transaction detail apart."
        action={<button onClick={loadSummary}>Refresh Activity</button>}
      />

      {error ? <div className="error-banner">{error}</div> : null}

      <section className="panel">
        <h2>Recent Records</h2>
        <div className="list">
          {(summary?.recentActivity || []).map((record) => (
            <div className="list-item" key={record.id}>
              <strong>{record.category}</strong>
              <span>{record.type}</span>
              <span>{record.transactionDate}</span>
              <span>{formatCurrency(record.amount)}</span>
              <span>{record.notes || "-"}</span>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

export default ActivityPage;
