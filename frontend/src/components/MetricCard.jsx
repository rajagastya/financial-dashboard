import { formatCurrency } from "../utils/format";

function MetricCard({ title, value }) {
  return (
    <div className="panel metric-card">
      <span>{title}</span>
      <strong>{formatCurrency(value || 0)}</strong>
    </div>
  );
}

export default MetricCard;
