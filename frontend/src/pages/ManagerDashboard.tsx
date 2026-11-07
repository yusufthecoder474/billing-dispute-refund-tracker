import { useEffect, useState, type FormEvent } from "react";
import { api, errorMessage } from "../api/client";
import { utc, money, type Sla, type Weekly } from "../api/types";
import { Notice } from "../components/Notice";
function monday() {
  const d = new Date();
  d.setUTCDate(d.getUTCDate() - ((d.getUTCDay() + 6) % 7));
  return d.toISOString().slice(0, 10);
}
export function ManagerDashboard() {
  const [summary, setSummary] = useState<Sla>(),
    [report, setReport] = useState<Weekly>(),
    [week, setWeek] = useState(monday),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [retry, setRetry] = useState(0);
  useEffect(() => {
    let active = true;
    setError("");
    api<Sla>("/sla/summary")
      .then((x) => {
        if (active) setSummary(x);
      })
      .catch((e) => {
        if (active) setError(errorMessage(e));
      });
    return () => {
      active = false;
    };
  }, [retry]);
  async function load(e: FormEvent) {
    e.preventDefault();
    setError("");
    setReport(undefined);
    setBusy(true);
    try {
      setReport(await api<Weekly>("/reports/weekly?week=" + week));
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="card">
      <div className="title-row">
        <h1>Manager dashboard</h1>
        <button onClick={() => setRetry((v) => v + 1)}>Refresh SLA</button>
      </div>
      <Notice error={error} />
      {summary ? (
        <>
          <p>
            Snapshot: {utc(summary.asOf)}. Deadlines include weekends and
            holidays.
          </p>
          <div className="metrics">
            {Object.entries({
              Open: summary.open,
              "Due within 12h": summary.dueSoon,
              Overdue: summary.overdue,
              Resolved: summary.resolved,
              "Resolved within SLA": summary.resolvedWithinSla,
              "Resolved breached": summary.resolvedBreached,
            }).map(([label, value]) => (
              <article key={label}>
                <span>{label}</span>
                <strong>{value}</strong>
              </article>
            ))}
          </div>
          <p>Due-soon and overdue counts are subsets of open disputes.</p>
        </>
      ) : (
        !error && <p role="status">Loading SLA summary…</p>
      )}
      <h2>Weekly report</h2>
      <form onSubmit={load}>
        <label>
          Week starting Monday (UTC)
          <input
            type="date"
            required
            value={week}
            onChange={(e) => {
              setWeek(e.target.value);
              setReport(undefined);
            }}
          />
        </label>
        <button disabled={busy}>
          {busy ? "Generating…" : "Generate report"}
        </button>
      </form>
      {report && (
        <>
          <p>
            {utc(report.start)} to {utc(report.end)} (end excluded)
          </p>
          <dl>
            {Object.entries({
              Created: report.created,
              Resolved: report.resolved,
              "Within SLA": report.withinSla,
              Breached: report.breached,
              "Average resolution hours": report.averageResolutionHours,
              "Approved refunds": money(report.approvedRefundTotal),
            }).map(([label, value]) => (
              <div key={label}>
                <dt>{label}</dt>
                <dd>{value}</dd>
              </div>
            ))}
          </dl>
          <a
            className="button"
            href={"/api/reports/weekly.csv?week=" + report.start.slice(0, 10)}
          >
            Download CSV
          </a>
        </>
      )}
    </section>
  );
}
