import { useEffect, useRef, useState } from 'react';
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
  const isUnsaved = !invitation?.id;
  const status = invitation?.status;
  const isDraft = isUnsaved || status === 'DRAFT';
  const canSave = isUnsaved || status === 'DRAFT';
  const canSend = Boolean(invitation?.id && status === 'DRAFT');
  const canCancel = Boolean(invitation?.id && (status === 'DRAFT' || status === 'SENT'));
  const readOnly = !canSave;
  const fieldRefs = {
    clientType: useRef(null),
    preferredName: useRef(null),
    email: useRef(null)
  };
  const [form, setForm] = useState(() => ({
    preferredName: invitation?.preferredName || '',
    email: invitation?.email || '',
    clientType: invitation?.clientType || 'INDIVIDUAL'
  }));
  const [validationErrors, setValidationErrors] = useState({});

  const title = isUnsaved ? 'New Invitation' : isDraft ? 'Edit Invitation' : 'View Invitation';

  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === 'Escape' && !document.querySelector('.confirmation-backdrop')) {
        onClose();
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  useEffect(() => {
    setForm({
      preferredName: invitation?.preferredName || '',
      email: invitation?.email || '',
      clientType: invitation?.clientType || 'INDIVIDUAL'
    });
    setValidationErrors({});
  }, [invitation?.id]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: value
    }));
    setValidationErrors((current) => ({
      ...current,
      [name]: undefined
    }));
  }

  function submit(event) {
    event.preventDefault();

    if (!canSave) {
      return;
    }

    const payload = validateAndBuildPayload();
    if (!payload) {
      return;
    }

    if (isUnsaved) {
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

  function validateAndBuildPayload() {
    const payload = buildPayload();
    const errors = {};

    if (!payload.clientType) {
      errors.clientType = 'Client Type is required.';
    }

    if (!payload.preferredName) {
      errors.preferredName = 'Preferred Name is required.';
    }

    if (!payload.email) {
      errors.email = 'Email is required.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.email)) {
      errors.email = 'Enter a valid email address.';
    }

    setValidationErrors(errors);

    const firstInvalidField = ['clientType', 'preferredName', 'email'].find((fieldName) => errors[fieldName]);
    if (firstInvalidField) {
      fieldRefs[firstInvalidField].current?.focus();
      return null;
    }

    return payload;
  }

  function sendDraftInvitation() {
    const payload = validateAndBuildPayload();
    if (!payload) {
      return;
    }

    if (canSend) {
      onSend(invitation.id, payload);
    }
  }

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
      <div className="modal-panel">
        <div className="modal-header">
          <div>
            <h2 id="modalTitle">{title}</h2>
            <div className={`modal-subtitle ${invitation ? '' : 'is-hidden'}`} aria-hidden={!invitation}>
              <StatusBadge status={invitation?.status || 'DRAFT'} />
            </div>
          </div>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close" disabled={saving}>x</button>
        </div>
        <form className="form-grid" onSubmit={submit} noValidate>
          <label className="field">
            <span>Client Type</span>
            <select
              ref={fieldRefs.clientType}
              name="clientType"
              value={form.clientType}
              onChange={updateField}
              required
              disabled={readOnly}
              aria-invalid={Boolean(validationErrors.clientType)}
            >
              <option value="INDIVIDUAL">Individual</option>
              <option value="COMPANY">Company</option>
            </select>
            {validationErrors.clientType && <span className="field-error">{validationErrors.clientType}</span>}
          </label>
          <label className="field">
            <span>Preferred Name</span>
            <input
              ref={fieldRefs.preferredName}
              name="preferredName"
              value={form.preferredName}
              onChange={updateField}
              maxLength="255"
              required
              readOnly={readOnly}
              autoFocus={!readOnly}
              aria-invalid={Boolean(validationErrors.preferredName)}
            />
            {validationErrors.preferredName && <span className="field-error">{validationErrors.preferredName}</span>}
          </label>
          <label className="field">
            <span>Email</span>
            <input
              ref={fieldRefs.email}
              name="email"
              type="email"
              value={form.email}
              onChange={updateField}
              maxLength="255"
              required
              readOnly={readOnly}
              aria-invalid={Boolean(validationErrors.email)}
            />
            {validationErrors.email && <span className="field-error">{validationErrors.email}</span>}
          </label>

          <div className="details-grid">
            <Detail label="Created" value={formatDate(invitation?.createdAt)} />
            <Detail label="Expires" value={formatDate(invitation?.expiresAt)} />
          </div>

          <div className="form-actions">
            <button className="button primary-button" type="submit" disabled={saving || !canSave}>
              {saving ? 'Saving...' : 'Save'}
            </button>
            <Button variant="secondary" onClick={sendDraftInvitation} disabled={saving || !canSend}>
              Send Invitation
            </Button>
            <Button variant="danger" onClick={() => onCancel(invitation.id)} disabled={saving || !canCancel}>
              Cancel Invitation
            </Button>
            <Button variant="secondary" onClick={onClose} disabled={saving}>Close</Button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Detail({ label, value }) {
  return (
    <div className="detail">
      <span>{label}</span>
      <strong className="detail-value">{value || ''}</strong>
    </div>
  );
}
