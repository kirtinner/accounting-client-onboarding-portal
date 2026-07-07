import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import StatusBadge from './StatusBadge.jsx';
import { formatDate } from '../../../utils/formatters.js';

export default function InvitationDetailsModal({
  mode,
  invitation,
  saving,
  onClose,
  onCreate,
  onUpdate,
  onSend,
  onCancel
}) {
  const isCreate = mode === 'create';
  const isDraft = isCreate || invitation?.status === 'DRAFT';
  const canCancel = invitation?.status === 'DRAFT' || invitation?.status === 'SENT';
  const readOnly = !isDraft;
  const [form, setForm] = useState(() => ({
    preferredName: invitation?.preferredName || '',
    email: invitation?.email || '',
    clientType: invitation?.clientType || 'INDIVIDUAL'
  }));

  const title = isCreate ? 'New Invitation' : isDraft ? 'Edit Invitation' : 'View Invitation';

  function updateField(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value
    }));
  }

  function submit(event) {
    event.preventDefault();

    const payload = buildPayload();

    if (isCreate) {
      onCreate(payload);
      return;
    }

    if (invitation?.id && isDraft) {
      onUpdate(invitation.id, payload);
    }
  }

  function buildPayload() {
    return {
      preferredName: form.preferredName.trim(),
      email: form.email.trim(),
      clientType: form.clientType
    };
  }

  function sendDraftInvitation(event) {
    if (!event.currentTarget.form.reportValidity()) {
      return;
    }

    if (invitation?.id && isDraft) {
      onSend(invitation.id, buildPayload());
    }
  }

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
      <div className="modal-panel">
        <div className="modal-header">
          <div>
            <h2 id="modalTitle">{title}</h2>
            {!isCreate && invitation && (
              <div className="modal-subtitle">
                <StatusBadge status={invitation.status} />
              </div>
            )}
          </div>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close" disabled={saving}>x</button>
        </div>
        <form className="form-grid" onSubmit={submit}>
          <label className="field">
            <span>Preferred Name</span>
            <input
              name="preferredName"
              value={form.preferredName}
              onChange={updateField}
              maxLength="255"
              required
              readOnly={readOnly}
              autoFocus={!readOnly}
            />
          </label>
          <label className="field">
            <span>Email</span>
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={updateField}
              maxLength="255"
              required
              readOnly={readOnly}
            />
          </label>
          <label className="field">
            <span>Client Type</span>
            <select name="clientType" value={form.clientType} onChange={updateField} required disabled={readOnly}>
              <option value="INDIVIDUAL">Individual</option>
              <option value="COMPANY">Company</option>
            </select>
          </label>

          {!isCreate && invitation && (
            <div className="details-grid">
              <Detail label="Created" value={formatDate(invitation.createdAt)} />
              <Detail label="Expires" value={formatDate(invitation.expiresAt)} />
              <Detail label="Sent" value={formatDate(invitation.sentAt)} />
              <Detail label="Cancelled" value={formatDate(invitation.cancelledAt)} />
            </div>
          )}

          <div className="form-actions">
            {isDraft && (
              <button className="button primary-button" type="submit" disabled={saving}>
                {saving ? 'Saving...' : 'Save'}
              </button>
            )}
            {isDraft && !isCreate && (
              <Button variant="secondary" onClick={sendDraftInvitation} disabled={saving}>
                Send Invitation
              </Button>
            )}
            {canCancel && !isCreate && (
              <Button variant="danger" onClick={() => onCancel(invitation.id)} disabled={saving}>
                Cancel Invitation
              </Button>
            )}
            <Button variant="secondary" onClick={onClose} disabled={saving}>Close</Button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Detail({ label, value }) {
  if (!value) {
    return null;
  }

  return (
    <div className="detail">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
