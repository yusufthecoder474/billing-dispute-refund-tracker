import { useState, type FormEvent } from "react";
import { api, errorMessage } from "../api/client";
import type { Dispute } from "../api/types";
import { Notice } from "../components/Notice";
export function CreateDispute({
  onCreated,
}: {
  onCreated: (id: number) => void;
}) {
  const [category, setCategory] = useState("DUPLICATE_CHARGE"),
    [description, setDescription] = useState(""),
    [amount, setAmount] = useState(""),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  async function submit(e: FormEvent) {
    e.preventDefault();
    setError("");
    if (description.trim().length < 10) {
      setError("Describe the issue in at least 10 characters.");
      return;
    }
    setBusy(true);
    try {
      const d = await api<Dispute>("/disputes", "POST", {
        category,
        description,
        disputedAmount: amount,
      });
      onCreated(d.id);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="card">
      <h1>Submit a billing dispute</h1>
      <p>
        Your deadline is 72 calendar hours after submission, including weekends.
      </p>
      <Notice error={error} />
      <form onSubmit={submit}>
        <label>
          Category
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            {[
              "DUPLICATE_CHARGE",
              "INCORRECT_AMOUNT",
              "UNEXPECTED_FEE",
              "BILLING_ADJUSTMENT",
            ].map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>
        </label>
        <label>
          Disputed amount (INR)
          <input
            type="number"
            min="0.01"
            max="9999999999.99"
            step="0.01"
            required
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />
        </label>
        <label>
          Description
          <textarea
            minLength={10}
            maxLength={2000}
            required
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </label>
        <button disabled={busy}>
          {busy ? "Submitting…" : "Submit dispute"}
        </button>
      </form>
    </section>
  );
}
