import { apiRequest } from '../../../services/apiClient.js';

const API_BASE = '/api/questionnaires';

export function getQuestionnaires(invitationStatuses = []) {
  const searchParams = new URLSearchParams();

  invitationStatuses.forEach((status) => {
    searchParams.append('statuses', status);
  });

  const queryString = searchParams.toString();
  const url = queryString ? `${API_BASE}?${queryString}` : API_BASE;

  return apiRequest(url);
}

export function getQuestionnaire(id) {
  return apiRequest(`${API_BASE}/${id}`);
}

export function updateQuestionnaire(id, payload) {
  return apiRequest(`${API_BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function approveQuestionnaire(id) {
  return apiRequest(`${API_BASE}/${id}/approve`, { method: 'POST' });
}

export function reopenQuestionnaire(id) {
  return apiRequest(`${API_BASE}/${id}/reopen`, { method: 'POST' });
}

export function sendQuestionnaireToXpm(id) {
  return apiRequest(`${API_BASE}/${id}/send-to-xpm`, { method: 'POST' });
}
