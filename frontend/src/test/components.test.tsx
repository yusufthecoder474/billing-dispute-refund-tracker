import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { vi, describe, it, expect, beforeEach } from "vitest";
import { Login } from "../pages/Login";
import { CreateDispute } from "../pages/CreateDispute";
import { DisputeList } from "../pages/DisputeList";
import { DisputeDetail } from "../pages/DisputeDetail";
import { Pagination } from "../components/Pagination";
import { api, csrf, ApiError } from "../api/client";
vi.mock("../api/client", async (importOriginal) => ({
  ...(await importOriginal<typeof import("../api/client")>()),
  api: vi.fn(),
  csrf: vi.fn(),
}));
const mockApi = vi.mocked(api);
beforeEach(() => {
  vi.resetAllMocks();
  vi.mocked(csrf).mockResolvedValue({
    token: "test",
    headerName: "X-CSRF-TOKEN",
  });
});
describe("Forms and workflow feedback", () => {
  it("submits login and loads authenticated user", async () => {
    const done = vi.fn();
    mockApi
      .mockResolvedValueOnce({ authenticated: true })
      .mockResolvedValueOnce({ id: 1, role: "CUSTOMER" });
    render(<Login onLogin={done} />);
    fireEvent.change(screen.getByLabelText("Email"), {
      target: { value: "customer@demo.local" },
    });
    fireEvent.change(screen.getByLabelText("Password"), {
      target: { value: "DemoPass123!" },
    });
    fireEvent.submit(
      screen.getByRole("button", { name: "Sign in" }).closest("form")!,
    );
    await waitFor(() => expect(done).toHaveBeenCalled());
    expect(csrf).toHaveBeenCalled();
  });
  it("shows invalid login error", async () => {
    mockApi.mockRejectedValue(new ApiError(401, "Invalid credentials."));
    render(<Login onLogin={vi.fn()} />);
    fireEvent.submit(
      screen.getByRole("button", { name: "Sign in" }).closest("form")!,
    );
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Invalid credentials",
    );
  });
  it("rejects whitespace-only descriptions", async () => {
    render(<CreateDispute onCreated={vi.fn()} />);
    fireEvent.change(screen.getByLabelText("Description"), {
      target: { value: "          " },
    });
    fireEvent.submit(
      screen.getByRole("button", { name: "Submit dispute" }).closest("form")!,
    );
    expect(screen.getByRole("alert")).toHaveTextContent("at least 10");
    expect(mockApi).not.toHaveBeenCalled();
  });
  it("creates a dispute with decimal string amount", async () => {
    mockApi.mockResolvedValue({ id: 42 });
    const done = vi.fn();
    render(<CreateDispute onCreated={done} />);
    fireEvent.change(screen.getByLabelText("Description"), {
      target: { value: "Incorrect duplicate charge" },
    });
    fireEvent.change(screen.getByLabelText("Disputed amount (INR)"), {
      target: { value: "75.50" },
    });
    fireEvent.submit(
      screen.getByRole("button", { name: "Submit dispute" }).closest("form")!,
    );
    await waitFor(() => expect(done).toHaveBeenCalledWith(42));
    expect(mockApi).toHaveBeenCalledWith(
      "/disputes",
      "POST",
      expect.objectContaining({ disputedAmount: "75.50" }),
    );
  });
  it("renders empty queue", async () => {
    mockApi.mockResolvedValue({
      content: [],
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
    });
    render(<DisputeList role="SUPPORT" onOpen={vi.fn()} />);
    expect(
      await screen.findByText("No disputes match these filters."),
    ).toBeInTheDocument();
  });
  it("shows loading and network failures", async () => {
    mockApi.mockRejectedValue(new Error("Network error."));
    render(<DisputeList role="CUSTOMER" onOpen={vi.fn()} />);
    expect(screen.getByRole("status")).toHaveTextContent("Loading");
    expect(await screen.findByRole("alert")).toHaveTextContent("Network error");
  });
  it("paginates and disables boundaries", () => {
    const change = vi.fn();
    render(<Pagination page={0} total={2} onChange={change} />);
    expect(screen.getByText("Previous")).toBeDisabled();
    fireEvent.click(screen.getByText("Next"));
    expect(change).toHaveBeenCalledWith(1);
  });
  it("shows conflict without pretending the action succeeded", async () => {
    const d = {
      id: 1,
      version: 0,
      customerId: 1,
      assignedSupportId: 2,
      category: "DUPLICATE_CHARGE",
      description: "Duplicate charge",
      disputedAmount: 100,
      status: "OPEN",
      createdAt: "2026-11-02T00:00:00Z",
      dueAt: "2026-11-05T00:00:00Z",
    };
    mockApi.mockImplementation(async (path, method) => {
      if (method === "POST")
        throw new ApiError(
          409,
          "This dispute changed. Reload it before trying again.",
        );
      return path.includes("/audit")
        ? { content: [], totalPages: 0 }
        : { dispute: d, decision: null };
    });
    render(
      <DisputeDetail
        id={1}
        user={{
          id: 2,
          email: "support@demo.local",
          displayName: "Support",
          role: "SUPPORT",
        }}
      />,
    );
    fireEvent.click(await screen.findByText("Start review"));
    expect(await screen.findByRole("alert")).toHaveTextContent("Reload");
    expect(screen.queryByText("Action saved.")).not.toBeInTheDocument();
  });
});
