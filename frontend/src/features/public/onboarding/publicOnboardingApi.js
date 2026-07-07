import { apiRequest } from '../../../services/apiClient.js';

export function getPublicOnboarding(token) {
  return apiRequest(`/api/public/onboarding/${encodeURIComponent(token)}`);
}
