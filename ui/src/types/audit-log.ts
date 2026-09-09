export interface AuditLog {
  id: number;
  action: string;
  entity: string;
  entityId: number;
  performedByEmail: string;
  details: string;
  timestamp: string;
}
