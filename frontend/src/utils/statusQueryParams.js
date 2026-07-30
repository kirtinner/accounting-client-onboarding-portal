export function parseStatusQuery(search, allowedStatuses) {
  const params = new URLSearchParams(search);
  const requestedStatuses = params
    .getAll('statuses')
    .flatMap((value) => value.split(','))
    .map((value) => value.trim())
    .filter(Boolean);
  const requestedStatusSet = new Set(requestedStatuses);

  return normalizeStatuses(requestedStatusSet, allowedStatuses);
}

export function normalizeStatuses(statuses, allowedStatuses) {
  const statusSet = new Set(statuses);

  return allowedStatuses.filter((status) => statusSet.has(status));
}

export function buildUrlWithStatusQuery(pathname, search, statuses) {
  const params = new URLSearchParams(search);
  params.delete('statuses');

  const queryParts = [];
  const remainingQuery = params.toString();

  if (statuses.length > 0) {
    queryParts.push(`statuses=${statuses.join(',')}`);
  }

  if (remainingQuery) {
    queryParts.push(remainingQuery);
  }

  return queryParts.length > 0
    ? `${pathname}?${queryParts.join('&')}`
    : pathname;
}

export function replaceStatusQuery(pathname, search, statuses) {
  window.history.replaceState(
    {},
    '',
    buildUrlWithStatusQuery(pathname, search, statuses)
  );
}

export function pushStatusQuery(pathname, search, statuses) {
  window.history.pushState(
    {},
    '',
    buildUrlWithStatusQuery(pathname, search, statuses)
  );
}
