const AUSTRALIAN_POSTCODE_RANGES = [
  { start: 800, end: 999, state: 'NT' },
  { start: 1000, end: 2599, state: 'NSW' },
  { start: 2600, end: 2618, state: 'ACT' },
  { start: 2619, end: 2899, state: 'NSW' },
  { start: 2900, end: 2920, state: 'ACT' },
  { start: 2921, end: 2999, state: 'NSW' },
  { start: 3000, end: 3999, state: 'VIC' },
  { start: 4000, end: 4999, state: 'QLD' },
  { start: 5000, end: 5799, state: 'SA' },
  { start: 6000, end: 6797, state: 'WA' },
  { start: 7000, end: 7999, state: 'TAS' }
];

export const AUSTRALIAN_STATES = ['VIC', 'NSW', 'QLD', 'SA', 'WA', 'TAS', 'ACT', 'NT'];

export function lookupAustralianPostcode(postcode) {
  const normalized = String(postcode || '').trim();
  if (!/^\d{4}$/.test(normalized)) {
    return { postcode: normalized, state: null, suburbs: [] };
  }

  const postcodeNumber = Number(normalized);
  const range = AUSTRALIAN_POSTCODE_RANGES.find(({ start, end }) => (
    postcodeNumber >= start && postcodeNumber <= end
  ));

  return {
    postcode: normalized,
    state: range?.state || null,
    suburbs: []
  };
}
