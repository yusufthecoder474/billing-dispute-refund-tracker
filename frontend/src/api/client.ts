export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}
let token: { token: string; headerName: string } | undefined;
export async function csrf() {
  const r = await fetch("/api/auth/csrf", { credentials: "same-origin" });
  if (!r.ok) throw new Error("Cannot establish a secure session.");
  token = (await r.json()) as { token: string; headerName: string };
  return token;
}
export async function api<T>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T> {
  const headers: Record<string, string> = {};
  if (method !== "GET") {
    const t = token ?? (await csrf());
    headers[t.headerName] = t.token;
  }
  let payload: BodyInit | undefined;
  if (body instanceof URLSearchParams) {
    headers["Content-Type"] = "application/x-www-form-urlencoded";
    payload = body;
  } else if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    payload = JSON.stringify(body);
  }
  let r: Response;
  try {
    r = await fetch("/api" + path, {
      method,
      headers,
      body: payload,
      credentials: "same-origin",
    });
  } catch {
    throw new Error("Network error. Check your connection and try again.");
  }
  if (!r.ok) {
    const error = (await r
      .json()
      .catch(() => ({ message: "Request failed." }))) as { message?: string };
    if (r.status === 401) window.dispatchEvent(new Event("session-expired"));
    if (r.status === 403) token = undefined;
    throw new ApiError(
      r.status,
      r.status === 409
        ? "This dispute changed. Reload it before trying again."
        : (error.message ?? "Request failed."),
    );
  }
  if (path === "/auth/login" || path === "/auth/logout") token = undefined;
  return r.status === 204 ? (undefined as T) : ((await r.json()) as T);
}
export function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Something went wrong.";
}
