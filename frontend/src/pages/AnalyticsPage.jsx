import { useEffect, useState } from "react";
import { api } from "../api";
import PageHeader from "../components/PageHeader";
import { formatCurrency } from "../utils/format";

function AnalyticsPage() {
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
        eyebrow="Analytics"
        title="Category totals and trend analysis"
        subtitle="This page focuses only on aggregated dashboard insights."
        action={<button onClick={loadSummary}>Refresh Analytics</button>}
      />

      {error ? <div className="error-banner">{error}</div> : null}

      <section className="grid content-grid">
        <div className="panel">
          <h2>Category Expense Totals</h2>
          <div className="list">
            {(summary?.categoryTotals || []).map((item) => (
              <div className="list-item" key={item.category}>
                <strong>{item.category}</strong>
                <span>{formatCurrency(item.total)}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="panel">
          <h2>Trend Snapshot</h2>
          <p className="helper">
            Compare income and expense movement month over month for dashboard analysis.
          </p>
        </div>
      </section>

      <section className="panel">
        <h2>Monthly Trends</h2>
        <div className="list">
          {(summary?.monthlyTrends || []).map((trend) => (
            <div className="list-item" key={trend.period}>
              <strong>{trend.period}</strong>
              <span>Income: {formatCurrency(trend.income)}</span>
              <span>Expense: {formatCurrency(trend.expense)}</span>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

export default AnalyticsPage;
