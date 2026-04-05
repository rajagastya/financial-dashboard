import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import PageHeader from "../components/PageHeader";

const emptyRecord = {
  amount: "",
  type: "EXPENSE",
  category: "",
  transactionDate: "",
  notes: "",
};

function RecordFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = useMemo(() => Boolean(id), [id]);
  const [form, setForm] = useState(emptyRecord);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isEdit) {
      loadRecord();
    }
  }, [id]);

  async function loadRecord() {
    setError("");
    try {
      const record = await api.getRecord(id);
      setForm({
        amount: String(record.amount),
        type: record.type,
        category: record.category,
        transactionDate: record.transactionDate,
        notes: record.notes || "",
      });
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    const payload = {
      ...form,
      amount: Number(form.amount),
    };

    try {
      if (isEdit) {
        await api.updateRecord(id, payload);
      } else {
        await api.createRecord(payload);
      }
      navigate("/app/records");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Record Form"
        title={isEdit ? "Edit financial record" : "Create financial record"}
        subtitle="A dedicated page keeps record authoring separate from the records list."
      />

      {error ? <div className="error-banner">{error}</div> : null}

      <section className="panel form-panel">
        <form className="form-grid" onSubmit={handleSubmit}>
          <input
            type="number"
            min="0.01"
            step="0.01"
            placeholder="Amount"
            value={form.amount}
            onChange={(event) => setForm({ ...form, amount: event.target.value })}
            required
          />
          <select value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value })}>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
          <input
            placeholder="Category"
            value={form.category}
            onChange={(event) => setForm({ ...form, category: event.target.value })}
            required
          />
          <input
            type="date"
            value={form.transactionDate}
            onChange={(event) => setForm({ ...form, transactionDate: event.target.value })}
            required
          />
          <textarea
            placeholder="Notes"
            value={form.notes}
            onChange={(event) => setForm({ ...form, notes: event.target.value })}
          />
          <div className="button-row">
            <button type="button" className="ghost-button" onClick={() => navigate("/app/records")}>
              Cancel
            </button>
            <button type="submit">{isEdit ? "Update Record" : "Create Record"}</button>
          </div>
        </form>
      </section>
    </>
  );
}

export default RecordFormPage;
