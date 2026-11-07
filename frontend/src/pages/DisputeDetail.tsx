import { useEffect, useState, type FormEvent } from "react";
import { api, errorMessage } from "../api/client";
import {
  utc,
  money,
  type Detail,
  type Audit,
  type Page,
  type User,
} from "../api/types";
import { Notice } from "../components/Notice";
import { Pagination } from "../components/Pagination";
export function DisputeDetail({ id, user }: { id: number; user: User }) {
  const [data, setData] = useState<Detail>(),
    [audit, setAudit] = useState<Page<Audit>>(),
    [agents, setAgents] = useState<User[]>([]),
    [page, setPage] = useState(0),
    [supportId, setSupportId] = useState(""),
    [outcome, setOutcome] = useState("APPROVED"),
    [refund, setRefund] = useState(""),
    [explanation, setExplanation] = useState(""),
    [error, setError] = useState(""),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false),
    [loading, setLoading] = useState(true),
    [revision, setRevision] = useState(0);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    Promise.all([
      api<Detail>("/disputes/" + id),
      api<Page<Audit>>(`/disputes/${id}/audit?page=${page}&size=10`),
      user.role === "MANAGER"
        ? api<User[]>("/support-agents")
        : Promise.resolve([]),
    ])
      .then(([d, a, s]) => {
        if (active) {
          setData(d);
          setAudit(a);
          setAgents(s);
        }
      })
      .catch((e) => {
        if (active) setError(errorMessage(e));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [id, user.role, page, revision]);
  async function action(path: string, body: Record<string, unknown> = {}) {
    if (!data) return;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      await api(`/disputes/${id}/${path}`, "POST", {
        ...body,
        version: data.dispute.version,
      });
      setMessage("Action saved.");
      setRevision((v) => v + 1);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  function decision(e: FormEvent) {
    e.preventDefault();
    void action("decision", {
      outcome,
      approvedRefundAmount: outcome === "REJECTED" ? "0.00" : refund,
      explanation,
    });
  }
  if (!data)
    return (
      <section className="card">
        <Notice error={error} />
        {loading ? (
          <p role="status">Loading dispute…</p>
        ) : (
          <button onClick={() => setRevision((v) => v + 1)}>Retry</button>
        )}
      </section>
    );
  const d = data.dispute;
  return (
    <section className="card">
      <div className="title-row">
        <h1>Dispute #{d.id}</h1>
        <button
          disabled={busy}
          onClick={() => {
            setMessage("");
            setRevision((v) => v + 1);
          }}
        >
          Reload dispute
        </button>
      </div>
      <Notice error={error} message={message} />
      <p>
        <span className="badge">{d.status}</span> · {money(d.disputedAmount)} ·{" "}
        {d.category.replaceAll("_", " ")}
      </p>
      <p className="description">{d.description}</p>
      <dl>
        <dt>Created</dt>
        <dd>{utc(d.createdAt)}</dd>
        <dt>Deadline</dt>
        <dd>{utc(d.dueAt)}</dd>
        <dt>Assigned support ID</dt>
        <dd>{d.assignedSupportId ?? "Not assigned"}</dd>
        <dt>Version</dt>
        <dd>{d.version}</dd>
      </dl>
      {user.role === "MANAGER" && d.status === "OPEN" && (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            void action("assign", { supportId: Number(supportId) });
          }}
        >
          <h2>Assign support</h2>
          <label>
            Support agent
            <select
              required
              value={supportId}
              onChange={(e) => setSupportId(e.target.value)}
            >
              <option value="">Choose an agent</option>
              {agents.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.displayName}
                </option>
              ))}
            </select>
          </label>
          <button disabled={busy || loading}>Assign</button>
        </form>
      )}
      {user.role === "SUPPORT" && d.status === "OPEN" && (
        <button
          disabled={busy || loading}
          onClick={() => void action("start-review")}
        >
          Start review
        </button>
      )}
      {user.role === "SUPPORT" && d.status === "IN_REVIEW" && (
        <button
          disabled={busy || loading}
          onClick={() => void action("submit-for-decision")}
        >
          Submit for decision
        </button>
      )}
      {user.role === "MANAGER" && d.status === "PENDING_DECISION" && (
        <form onSubmit={decision}>
          <h2>Record final decision</h2>
          <p>This action is final. No money will be transferred.</p>
          <label>
            Outcome
            <select
              value={outcome}
              onChange={(e) => setOutcome(e.target.value)}
            >
              <option>APPROVED</option>
              <option>REJECTED</option>
            </select>
          </label>
          {outcome === "APPROVED" && (
            <label>
              Approved refund (INR)
              <input
                type="number"
                min="0"
                max={d.disputedAmount}
                step="0.01"
                required
                value={refund}
                onChange={(e) => setRefund(e.target.value)}
              />
            </label>
          )}
          <label>
            Explanation
            <textarea
              required
              minLength={10}
              maxLength={2000}
              value={explanation}
              onChange={(e) => setExplanation(e.target.value)}
            />
          </label>
          <button disabled={busy || loading}>Save final decision</button>
        </form>
      )}
      {data.decision && (
        <aside className="decision">
          <h2>Final decision: {data.decision.outcome}</h2>
          <p>{data.decision.explanation}</p>
          <p>
            Approved refund: {money(data.decision.approvedRefundAmount)}{" "}
            (simulation only)
          </p>
          <p>
            Resolved {utc(data.decision.createdAt)} ·{" "}
            {new Date(data.decision.createdAt) <= new Date(d.dueAt)
              ? "Within SLA"
              : "SLA breached"}
          </p>
        </aside>
      )}
      <h2>Audit timeline</h2>
      <ol className="timeline">
        {audit?.content.map((a) => (
          <li key={a.id}>
            <strong>{a.action.replaceAll("_", " ")}</strong>
            <p>
              {a.actor} · {utc(a.createdAt)}
            </p>
            <p>
              {a.previousStatus ?? "New"} → {a.newStatus}
            </p>
            <small>{a.detail}</small>
          </li>
        ))}
      </ol>
      {audit && (
        <Pagination page={page} total={audit.totalPages} onChange={setPage} />
      )}
    </section>
  );
}
