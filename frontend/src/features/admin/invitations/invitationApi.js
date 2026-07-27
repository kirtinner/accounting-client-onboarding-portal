import { apiRequest } from '../../../services/apiClient.js';

const API_BASE = '/api/invitations';

export function getInvitations(statuses = []) {
  const searchParams = new URLSearchParams();

  statuses.forEach((status) => {
    searchParams.append('statuses', status);
  });

  const queryString = searchParams.toString();
  const url = queryString ? `${API_BASE}?${queryString}` : API_BASE;

  return apiRequest(url);
}

export function createInvitation(payload) {
  return apiRequest(API_BASE, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateInvitation(id, payload) {
  return apiRequest(`${API_BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export async function sendInvitation(id, payload = null) {
  if (payload) {
    await updateInvitation(id, payload);
  }

  return apiRequest(`${API_BASE}/${id}/send`, { method: 'POST' });
}

export function cancelInvitation(id) {
  return apiRequest(`${API_BASE}/${id}/cancel`, { method: 'POST' });
}
