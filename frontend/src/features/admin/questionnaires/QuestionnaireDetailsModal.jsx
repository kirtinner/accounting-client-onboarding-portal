import { useEffect, useRef, useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import DateOfBirthPicker from '../../../components/common/DateOfBirthPicker.jsx';
import Message from '../../../components/common/Message.jsx';
import { AUSTRALIAN_STATES } from '../../../utils/australianPostcodes.js';
import { formatClientType, formatLocalDate } from '../../../utils/formatters.js';
import StatusBadge from '../invitations/StatusBadge.jsx';

export default function QuestionnaireDetailsModal({
  questionnaire,
  busy,
  actionMessage,
  onClose,
  onApprove,
  onReopen,
  onSendToXpm,
  onSave
}) {
  const status = questionnaire.invitationStatus;
  const approvalButtonLabel = status === 'APPROVED' ? 'Cancel Approval' : 'Approve';
  const canApprove = status === 'SUBMITTED';
  const canEdit = status === 'SUBMITTED';
  const canCancelApproval = status === 'APPROVED';
  const canSendToXpm = status === 'APPROVED';
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState(() => formFromQuestionnaire(questionnaire));
  const dateOfBirthRef = useRef(null);

  useEffect(() => {
    setForm(formFromQuestionnaire(questionnaire));
    setEditing(false);
  }, [questionnaire.questionnaireId]);

  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === 'Escape' && !document.querySelector('.confirmation-backdrop')) {
        onClose();
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: value
    }));
  }

  async function save() {
    const saved = await onSave(questionnaire, buildPayload(form));
    if (saved) {
      setEditing(false);
    }
  }

  function cancelEdit() {
    setForm(formFromQuestionnaire(questionnaire));
    setEditing(false);
  }

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="questionnaireModalTitle">
      <div className="modal-panel questionnaire-modal-panel">
        <div className="modal-header">
          <div>
            <h2 id="questionnaireModalTitle">Questionnaire Details</h2>
            <div className="modal-subtitle">
              <StatusBadge status={status} />
            </div>
          </div>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close">x</button>
        </div>

        <div className="questionnaire-detail-body">
          {actionMessage && <Message type="error">{actionMessage}</Message>}

          <DetailGroup
            title="Personal Details"
            editing={editing}
            dateOfBirthRef={dateOfBirthRef}
            fields={[
              {
                label: 'Client Type',
                name: 'clientType',
                value: questionnaire.clientType,
                displayValue: formatClientType(questionnaire.clientType),
                editable: false
              },
              { label: 'First Name', name: 'firstName', value: form.firstName, required: true },
              { label: 'Middle Name', name: 'middleName', value: form.middleName },
              { label: 'Last Name', name: 'lastName', value: form.lastName, required: true },
              {
                label: 'Date of Birth',
                name: 'dateOfBirth',
                type: 'date',
                value: form.dateOfBirth,
                displayValue: formatLocalDate(form.dateOfBirth),
                required: true
              }
            ]}
            onChange={updateField}
          />
          <DetailGroup
            title="Contact"
            editing={editing}
            fields={[
              { label: 'Email', name: 'email', type: 'email', value: form.email, required: true },
              { label: 'Mobile Number', name: 'mobilePhone', value: form.mobilePhone, required: true }
            ]}
            onChange={updateField}
          />
          <DetailGroup
            title="Residential Address"
            editing={editing}
            fields={[
              { label: 'Address Line 1', name: 'addressLine1', value: form.addressLine1, required: true },
              { label: 'Address Line 2', name: 'addressLine2', value: form.addressLine2 },
              { label: 'Suburb', name: 'suburb', value: form.suburb, required: true },
              { label: 'State', name: 'state', value: form.state, required: true, options: AUSTRALIAN_STATES },
              { label: 'Postcode', name: 'postcode', value: form.postcode, required: true },
              { label: 'Country', name: 'country', value: form.country, required: true }
            ]}
            onChange={updateField}
          />
        </div>

        <div className="modal-footer split-actions">
          {!editing && (
            <>
              <div className="modal-footer-left">
                <Button variant="secondary" className="modal-action-button" onClick={() => setEditing(true)} disabled={busy || !canEdit}>
                  Edit
                </Button>
              </div>
              <div className="modal-footer-right">
                <Button
                  variant={canApprove ? 'primary' : 'secondary'}
                  className="modal-action-button"
                  onClick={() => (canCancelApproval ? onReopen(questionnaire) : onApprove(questionnaire))}
                  disabled={busy || (!canApprove && !canCancelApproval)}
                >
                  {approvalButtonLabel}
                </Button>
                <Button
                  variant="primary"
                  className="modal-action-button"
                  onClick={() => onSendToXpm(questionnaire)}
                  disabled={busy || !canSendToXpm}
                >
                  Send to XPM
                </Button>
                <Button variant="secondary" className="modal-action-button" onClick={onClose} disabled={busy}>Close</Button>
              </div>
            </>
          )}

          {editing && (
            <div className="modal-footer-right">
              <Button variant="primary" className="modal-action-button" onClick={save} disabled={busy}>
                {busy ? 'Saving...' : 'Save'}
              </Button>
              <Button variant="secondary" className="modal-action-button" onClick={cancelEdit} disabled={busy}>
                Cancel
              </Button>
              <Button variant="secondary" className="modal-action-button" onClick={onClose} disabled={busy}>Close</Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function DetailGroup({ title, fields, editing, onChange, dateOfBirthRef }) {
  return (
    <section className="detail-section">
      <h3>{title}</h3>
      <dl>
        {fields.map((field) => (
          <div key={field.name}>
            <dt>{field.label}</dt>
            <dd>
              {editing && field.editable !== false ? (
                <EditableField field={field} onChange={onChange} dateOfBirthRef={dateOfBirthRef} />
              ) : (
                displayFieldValue(field)
              )}
            </dd>
          </div>
        ))}
      </dl>
    </section>
  );
}

function displayFieldValue(field) {
  const value = Object.hasOwn(field, 'displayValue') ? field.displayValue : field.value;
  if (value === null || value === undefined || String(value).trim() === '') {
    return '-';
  }

  return value;
}

function EditableField({ field, onChange, dateOfBirthRef }) {
  if (field.type === 'date') {
    return (
      <DateOfBirthPicker
        value={field.value}
        inputRef={dateOfBirthRef}
        onChange={(value) => onChange({ target: { name: field.name, value } })}
        showLabel={false}
        className="date-picker-field admin-date-picker-field"
        inputClassName="admin-date-picker-input"
        usePortal
      />
    );
  }

  if (field.options) {
    return (
      <select
        name={field.name}
        value={field.value}
        onChange={onChange}
        required={field.required}
      >
        <option value="">Select state</option>
        {field.options.map((option) => (
          <option key={option} value={option}>{option}</option>
        ))}
      </select>
    );
  }

  return (
    <input
      name={field.name}
      type={field.type || 'text'}
      value={field.value}
      onChange={onChange}
      required={field.required}
      maxLength={field.name === 'postcode' ? 20 : undefined}
    />
  );
}

function formFromQuestionnaire(questionnaire) {
  return {
    firstName: questionnaire.firstName || '',
    middleName: questionnaire.middleName || '',
    lastName: questionnaire.lastName || '',
    dateOfBirth: questionnaire.dateOfBirth || '',
    email: questionnaire.email || '',
    mobilePhone: questionnaire.mobilePhone || '',
    addressLine1: questionnaire.addressLine1 || '',
    addressLine2: questionnaire.addressLine2 || '',
    postcode: questionnaire.postcode || '',
    state: questionnaire.state || '',
    suburb: questionnaire.suburb || '',
    country: questionnaire.country || 'Australia'
  };
}

function buildPayload(form) {
  return {
    firstName: form.firstName.trim(),
    middleName: emptyToNull(form.middleName),
    lastName: form.lastName.trim(),
    dateOfBirth: form.dateOfBirth,
    email: form.email.trim(),
    mobilePhone: form.mobilePhone.trim(),
    addressLine1: form.addressLine1.trim(),
    addressLine2: emptyToNull(form.addressLine2),
    suburb: form.suburb.trim(),
    state: form.state.trim(),
    postcode: form.postcode.trim(),
    country: form.country.trim()
  };
}

function emptyToNull(value) {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}
