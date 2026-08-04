export function formatDate(value) {
  if (!value) {
    return '';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return new Intl.DateTimeFormat('en-AU', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

export function formatLocalDate(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value || '')) {
    return '';
  }

  const [year, month, day] = value.split('-');
  return [day, month, year].join('/');
}

export function formatStatus(status) {
  return (status || '').replaceAll('_', ' ');
}

export function statusClass(status) {
  return (status || '').toLowerCase().replaceAll('_', '-');
}

export function formatClientType(clientType) {
  if (clientType === 'INDIVIDUAL') {
    return 'Individual';
  }
  if (clientType === 'COMPANY') {
    return 'Company';
  }
  return clientType || '';
}
