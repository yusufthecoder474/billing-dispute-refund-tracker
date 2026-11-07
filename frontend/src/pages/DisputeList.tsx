import { useEffect, useState } from "react";
import { api, errorMessage } from "../api/client";
import {
  statuses,
  utc,
  money,
  type Dispute,
  type Page,
  type Role,
} from "../api/types";
import { Pagination } from "../components/Pagination";
import { Notice } from "../components/Notice";
export function DisputeList({
  role,
  onOpen,
}: {
  role: Role;
  onOpen: (id: number) => void;
}) {
  const [data, setData] = useState<Page<Dispute>>(),
    [page, setPage] = useState(0),
    [status, setStatus] = useState(""),
    [sort, setSort] = useState("createdAt"),
    [direction, setDirection] = useState("desc"),
    [sla, setSla] = useState(""),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(true),
    [retry, setRetry] = useState(0);
  useEffect(() => {
    let active = true;
    setBusy(true);
    setError("");
    const query = new URLSearchParams({
      page: String(page),
      size: "10",
      sort,
      direction,
    });
    if (status) query.set("status", status);
    if (sla) query.set("sla", sla);
    api<Page<Dispute>>("/disputes?" + query)
      .then((d) => {
        if (active) setData(d);
      })
      .catch((e) => {
        if (active) setError(errorMessage(e));
      })
      .finally(() => {
        if (active) setBusy(false);
      });
    return () => {
      active = false;
    };
  }, [page, status, sort, direction, sla, retry]);
  return (
    <section className="card">
      <div className="title-row">
        <h1>
          {role === "SUPPORT"
            ? "My support queue"
            : role === "CUSTOMER"
              ? "My disputes"
              : "All disputes"}
        </h1>
        <button onClick={() => setRetry(retry + 1)}>Refresh</button>
      </div>
      <div className="filters">
        <label>
          Status
          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All statuses</option>
            {statuses.map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
        </label>
        <label>
          Sort by
          <select
            value={sort}
            onChange={(e) => {
              setSort(e.target.value);
              setPage(0);
            }}
          >
            <option value="createdAt">Creation date</option>
            <option value="dueAt">Deadline</option>
            <option value="status">Status</option>
          </select>
        </label>
        <label>
          Order
          <select
            value={direction}
            onChange={(e) => setDirection(e.target.value)}
          >
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
        </label>
        <label>
          Deadline filter
          <select
            value={sla}
            onChange={(e) => {
              setSla(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All deadlines</option>
            <option value="DUE_SOON">Due within 12 hours</option>
            <option value="OVERDUE">Overdue</option>
          </select>
        </label>
      </div>
      <Notice error={error} />
      {busy ? (
        <p role="status">Loading disputes…</p>
      ) : data && !error ? (
        <>
          <p>{data.totalElements} disputes</p>
          {data.content.length === 0 ? (
            <p>No disputes match these filters.</p>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Dispute</th>
                    <th>Category</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Deadline (UTC)</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((d) => (
                    <tr key={d.id}>
                      <td>
                        <button className="link" onClick={() => onOpen(d.id)}>
                          #{d.id}
                        </button>
                      </td>
                      <td>{d.category.replaceAll("_", " ")}</td>
                      <td>{money(d.disputedAmount)}</td>
                      <td>
                        <span className="badge">{d.status}</span>
                      </td>
                      <td>{utc(d.dueAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <Pagination page={page} total={data.totalPages} onChange={setPage} />
        </>
      ) : null}
    </section>
  );
}
