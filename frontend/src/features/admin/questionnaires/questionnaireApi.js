import { apiRequest } from '../../../services/apiClient.js';

const API_BASE = '/api/questionnaires';

export function getQuestionnaires() {
  return apiRequest(API_BASE);
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
