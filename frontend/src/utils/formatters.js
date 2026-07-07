export function formatDate(value) {
  if (!value) {
    return '';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
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
