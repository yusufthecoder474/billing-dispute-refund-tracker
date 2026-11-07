import { useEffect, useState } from "react";
import { api, errorMessage, ApiError } from "./api/client";
import type { User } from "./api/types";
import { Login } from "./pages/Login";
import { CreateDispute } from "./pages/CreateDispute";
import { DisputeList } from "./pages/DisputeList";
import { DisputeDetail } from "./pages/DisputeDetail";
import { ManagerDashboard } from "./pages/ManagerDashboard";
import { Notice } from "./components/Notice";
export default function App() {
  const [user, setUser] = useState<User>(),
    [ready, setReady] = useState(false),
    [view, setView] = useState("list"),
    [id, setId] = useState(0),
    [error, setError] = useState("");
  useEffect(() => {
    api<User>("/auth/me")
      .then(setUser)
      .catch((e) => {
        if (!(e instanceof ApiError && e.status === 401))
          setError(errorMessage(e));
      })
      .finally(() => setReady(true));
    const expired = () => {
      setUser(undefined);
      setView("list");
    };
    window.addEventListener("session-expired", expired);
    return () => window.removeEventListener("session-expired", expired);
  }, []);
  function open(id: number) {
    setId(id);
    setView("detail");
  }
  async function logout() {
    try {
      await api("/auth/logout", "POST");
      setUser(undefined);
      setView("list");
      setError("");
    } catch (e) {
      setError(errorMessage(e));
    }
  }
  return (
    <>
      <header>
        <div>
          <strong>Billing Dispute & Refund Tracker</strong>
          <small>SIMULATED APPROVALS · NO MONEY TRANSFERS</small>
        </div>
        {user && (
          <div>
            {user.displayName}
            <button onClick={() => void logout()}>Sign out</button>
          </div>
        )}
      </header>
      <main>
        <Notice error={error} />
        {!ready ? (
          <p role="status">Checking session…</p>
        ) : !user ? (
          <Login
            onLogin={(u) => {
              setUser(u);
              setError("");
              setView("list");
            }}
          />
        ) : (
          <>
            <nav className="main-nav" aria-label="Main navigation">
              <button
                aria-current={view === "list" ? "page" : undefined}
                onClick={() => setView("list")}
              >
                {user.role === "SUPPORT" ? "Support queue" : "Disputes"}
              </button>
              {user.role === "CUSTOMER" && (
                <button onClick={() => setView("create")}>New dispute</button>
              )}
              {user.role === "MANAGER" && (
                <button onClick={() => setView("dashboard")}>
                  SLA & reports
                </button>
              )}
            </nav>
            {view === "list" && <DisputeList role={user.role} onOpen={open} />}{" "}
            {view === "create" && user.role === "CUSTOMER" && (
              <CreateDispute onCreated={open} />
            )}{" "}
            {view === "detail" && (
              <DisputeDetail key={id} id={id} user={user} />
            )}{" "}
            {view === "dashboard" && user.role === "MANAGER" && (
              <ManagerDashboard />
            )}
          </>
        )}
      </main>
      <footer>Educational prototype · INR only · All timestamps UTC</footer>
    </>
  );
}
