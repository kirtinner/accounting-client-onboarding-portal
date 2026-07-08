import { apiRequest } from '../../../services/apiClient.js';

export function getPublicOnboarding(token) {
  return apiRequest(`/api/public/onboarding/${encodeURIComponent(token)}`);
}

export function savePublicQuestionnaire(token, payload) {
  return apiRequest(`/api/public/onboarding/${encodeURIComponent(token)}/questionnaire`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function submitPublicQuestionnaire(token) {
  return apiRequest(`/api/public/onboarding/${encodeURIComponent(token)}/submit`, {
    method: 'POST'
  });
}
