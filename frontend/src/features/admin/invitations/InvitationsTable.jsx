import StatusBadge from './StatusBadge.jsx';
import { formatClientType, formatDate } from '../../../utils/formatters.js';

export default function InvitationsTable({ invitations, loading, selectedInvitationId, onSelect, onOpen }) {
  let body;

  if (loading) {
    body = (
      <tr className="empty-row">
        <td colSpan="6" className="empty-cell">Loading invitations...</td>
      </tr>
    );
  } else if (invitations.length === 0) {
    body = (
      <tr className="empty-row">
        <td colSpan="6" className="empty-cell">No invitations yet. Create your first invitation.</td>
      </tr>
    );
  } else {
    body = invitations.map((invitation) => {
      const selected = invitation.id === selectedInvitationId;

      return (
        <tr
          key={invitation.id}
          className={selected ? 'selected-row' : ''}
          tabIndex="0"
          aria-selected={selected}
          onClick={() => onSelect(invitation.id)}
          onDoubleClick={() => onOpen(invitation)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              onOpen(invitation);
            }
          }}
        >
          <td>{invitation.preferredName || ''}</td>
          <td>{invitation.email || ''}</td>
          <td>{formatClientType(invitation.clientType)}</td>
          <td><StatusBadge status={invitation.status} /></td>
          <td>{formatDate(invitation.createdAt)}</td>
          <td>{formatDate(invitation.expiresAt)}</td>
        </tr>
      );
    });
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>Preferred Name</th>
            <th>Email</th>
            <th>Client Type</th>
            <th>Status</th>
            <th>Created</th>
            <th>Expires</th>
          </tr>
        </thead>
        <tbody>{body}</tbody>
      </table>
    </div>
  );
}
