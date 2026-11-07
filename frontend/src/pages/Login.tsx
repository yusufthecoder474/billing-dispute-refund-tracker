import { useState, type FormEvent } from "react";
import { api, csrf, errorMessage } from "../api/client";
import type { User } from "../api/types";
import { Notice } from "../components/Notice";
export function Login({ onLogin }: { onLogin: (u: User) => void }) {
  const [email, setEmail] = useState(""),
    [password, setPassword] = useState(""),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      await csrf();
      await api(
        "/auth/login",
        "POST",
        new URLSearchParams({ username: email, password }),
      );
      onLogin(await api<User>("/auth/me"));
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="login card">
      <p className="eyebrow">LOCAL DEMONSTRATION</p>
      <h1>A clear path from charge to decision.</h1>
      <p>Track billing disputes and simulated refund approvals in one place.</p>
      <Notice error={error} />
      <form onSubmit={submit}>
        <label>
          Email
          <input
            type="email"
            autoComplete="username"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>
        <label>
          Password
          <input
            type="password"
            autoComplete="current-password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        <button disabled={busy}>{busy ? "Signing in…" : "Sign in"}</button>
      </form>
      <details>
        <summary>Local demo accounts</summary>
        <p>customer@demo.local · support@demo.local · manager@demo.local</p>
        <p>
          Password for each: <code>DemoPass123!</code>
        </p>
        <p>Use only with synthetic records. No money is transferred.</p>
      </details>
    </section>
  );
}
