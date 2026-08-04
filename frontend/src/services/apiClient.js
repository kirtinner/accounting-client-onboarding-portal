export async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(options.headers || {})
    }
  });

  if (!response.ok) {
    throw await readApiError(response);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

async function readApiError(response) {
  try {
    const payload = await response.json();
    const error = new Error(payload.message || `Request failed with status ${response.status}`);
    error.status = response.status;
    error.code = payload.code || null;
    return error;
  } catch {
    const error = new Error(`Request failed with status ${response.status}`);
    error.status = response.status;
    error.code = null;
    return error;
  }
}
