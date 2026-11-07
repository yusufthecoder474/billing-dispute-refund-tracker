export type Role = "CUSTOMER" | "SUPPORT" | "MANAGER";
export type Status =
  | "OPEN"
  | "IN_REVIEW"
  | "PENDING_DECISION"
  | "RESOLVED_APPROVED"
  | "RESOLVED_REJECTED";
export interface User {
  id: number;
  email: string;
  displayName: string;
  role: Role;
}
export interface Dispute {
  id: number;
  customerId: number;
  assignedSupportId: number | null;
  category: string;
  description: string;
  disputedAmount: number;
  currency: string;
  status: Status;
  createdAt: string;
  dueAt: string;
  resolvedAt: string | null;
  version: number;
}
export interface Decision {
  outcome: string;
  approvedRefundAmount: number;
  explanation: string;
  createdAt: string;
  managerId: number;
}
export interface Detail {
  dispute: Dispute;
  decision: Decision | null;
}
export interface Audit {
  id: number;
  actor: string;
  actorId: number;
  action: string;
  previousStatus: Status | null;
  newStatus: Status;
  detail: string;
  createdAt: string;
}
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface Sla {
  open: number;
  dueSoon: number;
  overdue: number;
  resolved: number;
  resolvedWithinSla: number;
  resolvedBreached: number;
  asOf: string;
}
export interface Weekly {
  start: string;
  end: string;
  created: number;
  resolved: number;
  withinSla: number;
  breached: number;
  averageResolutionHours: number;
  approvedRefundTotal: number;
}
export const statuses: Status[] = [
  "OPEN",
  "IN_REVIEW",
  "PENDING_DECISION",
  "RESOLVED_APPROVED",
  "RESOLVED_REJECTED",
];
export const utc = (value: string) =>
  new Date(value).toISOString().replace("T", " ").replace(".000Z", " UTC");
export const money = (value: number) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(
    value,
  );
