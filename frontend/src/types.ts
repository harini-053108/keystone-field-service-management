export type Role = 'DISPATCHER' | 'TECHNICIAN' | 'MANAGER' | 'CUSTOMER';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type WorkOrderStatus =
  | 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CLOSED' | 'CANCELLED';
export type SlaStatus = 'ON_TRACK' | 'AT_RISK' | 'BREACHED' | 'NOT_APPLICABLE';

export interface AuthResponse {
  token: string;
  tokenType: string;
  id: number;
  email: string;
  fullName: string;
  role: Role;
  customerId: number | null;
}

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  customerId: number | null;
  enabled: boolean;
  createdAt: string;
}

export interface Customer {
  id: number;
  name: string;
  email: string | null;
  phone: string | null;
  address: string | null;
  createdAt: string;
  siteCount: number;
}

export interface Site {
  id: number;
  name: string;
  address: string | null;
  customerId: number;
  createdAt: string;
}

export interface Part {
  id: number;
  code: string;
  name: string;
  unitCost: number;
  stockQty: number;
  createdAt: string;
}

export interface StatusHistory {
  id: number;
  fromStatus: WorkOrderStatus | null;
  toStatus: WorkOrderStatus;
  changedById: number | null;
  changedByName: string | null;
  note: string | null;
  changedAt: string;
}

export interface PartUsage {
  id: number;
  partId: number;
  partCode: string;
  partName: string;
  quantity: number;
  unitCost: number;
  totalCost: number;
  note: string | null;
  createdAt: string;
}

export interface TimeLog {
  id: number;
  minutes: number;
  note: string | null;
  loggedById: number | null;
  loggedByName: string | null;
  loggedAt: string;
}

export interface WorkOrder {
  id: number;
  code: string;
  title: string;
  description: string | null;
  priority: Priority;
  status: WorkOrderStatus;
  slaStatus: SlaStatus;
  slaDueAt: string | null;
  customerId: number;
  customerName: string;
  siteId: number;
  siteName: string;
  assigneeId: number | null;
  assigneeName: string | null;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
  totalMinutes: number;
  totalPartsCost: number;
  partUsages: PartUsage[];
  timeLogs: TimeLog[];
  history: StatusHistory[];
}

export interface Paged<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Notification {
  id: number;
  message: string;
  type: string;
  workOrderId: number | null;
  read: boolean;
  createdAt: string;
}

export interface ReportSummary {
  totalOpen: number;
  overdue: number;
  breached: number;
  slaCompliancePercent: number;
  countsByStatus: Record<string, number>;
  countsBySla: Record<string, number>;
  byTechnician: { name: string; count: number }[];
  bySite: { name: string; count: number }[];
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}

export const PRIORITY_LABEL: Record<Priority, string> = {
  LOW: 'Low', MEDIUM: 'Medium', HIGH: 'High', URGENT: 'Urgent',
};