import { formatStatus, statusClass } from '../../../utils/formatters.js';

export default function StatusBadge({ status }) {
  return <span className={`status-badge status-${statusClass(status)}`}>{formatStatus(status)}</span>;
}
