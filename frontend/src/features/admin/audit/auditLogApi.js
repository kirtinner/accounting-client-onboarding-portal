import { apiRequest } from '../../../services/apiClient.js';

const API_BASE = '/api/audit-logs';

export function getAuditLogs() {
  return apiRequest(API_BASE);
}
