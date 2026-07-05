import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const API_BASE = '/api/invitations';

function App() {
  return (
    <AppLayout>
      <InvitationsPage />
    </AppLayout>
  );
}

function AppLayout({ children }) {
  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">
          <div className="brand-mark">A</div>
          <div>
            <div className="brand-name">Accounting Client Onboarding Portal</div>
            <div className="brand-context">Internal onboarding workspace</div>
          </div>
        </div>
      </header>
      <div className="app-body">
        <aside className="sidebar" aria-label="Primary navigation">
          <nav className="nav-list">
            <a className="nav-item active" href="/">Client Invitations</a>
            <span className="nav-item disabled" aria-disabled="true">Questionnaires</span>
            <span className="nav-item disabled" aria-disabled="true">Documents</span>
            <span className="nav-item disabled" aria-disabled="true">Xero</span>
          </nav>
        </aside>
        <main className="main-content">{children}</main>
      </div>
    </div>
  );
}

function InvitationsPage() {
  const [invitations, setInvitations] = useState([]);
  const [selectedInvitationId, setSelectedInvitationId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalMode, setModalMode] = useState(null);
  const [message, setMessage] = useState(null);

  const selectedInvitation = useMemo(
    () => invitations.find((invitation) => invitation.id === selectedInvitationId) || null,
    [invitations, selectedInvitationId]
  );

  const selectedButtonText = selectedInvitation?.status === 'DRAFT' ? 'Edit Selected' : 'View Selected';

  useEffect(() => {
    loadInvitations();
  }, []);

  async function loadInvitations(preferredSelectedId = selectedInvitationId) {
    setLoading(true);
    try {
      const data = await apiRequest(API_BASE);
      const nextInvitations = Array.isArray(data) ? data : [];
      setInvitations(nextInvitations);
      setSelectedInvitationId(() => {
        if (!preferredSelectedId) {
          return null;
        }
        return nextInvitations.some((invitation) => invitation.id === preferredSelectedId) ? preferredSelectedId : null;
      });
    } catch (error) {
      setInvitations([]);
      setSelectedInvitationId(null);
      setMessage({ type: 'error', text: error.message });
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setMessage(null);
    setModalMode('create');
  }

  function openSelectedInvitation(invitation = selectedInvitation) {
    if (!invitation) {
      return;
    }

    setSelectedInvitationId(invitation.id);
    setMessage(null);
    setModalMode('details');
  }

  async function createInvitation(payload) {
    setSaving(true);
    try {
      const createdInvitation = await apiRequest(API_BASE, {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      setModalMode(null);
      await loadInvitations(createdInvitation?.id || null);
      setMessage({ type: 'success', text: 'Invitation created.' });
    } catch (error) {
      setMessage({ type: 'error', text: error.message });
    } finally {
      setSaving(false);
    }
  }

  async function updateInvitation(id, payload) {
    setSaving(true);
    try {
      await apiRequest(`${API_BASE}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      setModalMode(null);
      await loadInvitations(id);
      setMessage({ type: 'success', text: 'Invitation updated.' });
    } catch (error) {
      setMessage({ type: 'error', text: error.message });
    } finally {
      setSaving(false);
    }
  }

  async function sendInvitation(id, payload = null) {
    setSaving(true);
    try {
      if (payload) {
        await apiRequest(`${API_BASE}/${id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
      }
      await apiRequest(`${API_BASE}/${id}/send`, { method: 'POST' });
      setModalMode(null);
      await loadInvitations(id);
      setMessage({ type: 'success', text: 'Invitation marked as sent.' });
    } catch (error) {
      setMessage({ type: 'error', text: error.message });
    } finally {
      setSaving(false);
    }
  }

  async function cancelInvitation(id) {
    if (!window.confirm('Cancel this invitation?')) {
      return;
    }

    setSaving(true);
    try {
      await apiRequest(`${API_BASE}/${id}/cancel`, { method: 'POST' });
      setModalMode(null);
      await loadInvitations(id);
      setMessage({ type: 'success', text: 'Invitation cancelled.' });
    } catch (error) {
      setMessage({ type: 'error', text: error.message });
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Client Invitations"
        description="Manage onboarding invitations sent to new clients."
        action={
          <div className="page-actions">
            <Button variant="secondary" onClick={() => loadInvitations()} disabled={loading || saving}>
              Refresh
            </Button>
            <Button variant="primary" onClick={openCreateModal} disabled={saving}>
              New Invitation
            </Button>
            <Button
              variant="secondary"
              onClick={() => openSelectedInvitation()}
              disabled={!selectedInvitation || loading || saving}
            >
              {selectedButtonText}
            </Button>
          </div>
        }
      />

      {message && <Message type={message.type}>{message.text}</Message>}

      <Card>
        <div className="card-header">
          <div>
            <h2>Invitations</h2>
            <p>Select a row to inspect it. Double-click to open details.</p>
          </div>
          {selectedInvitation && (
            <span className="selected-summary">
              Selected: {selectedInvitation.preferredName || selectedInvitation.email}
            </span>
          )}
        </div>
        <InvitationsTable
          invitations={invitations}
          loading={loading}
          selectedInvitationId={selectedInvitationId}
          onSelect={setSelectedInvitationId}
          onOpen={openSelectedInvitation}
        />
      </Card>

      {modalMode && (modalMode === 'create' || selectedInvitation) && (
        <InvitationDetailsModal
          mode={modalMode}
          invitation={modalMode === 'details' ? selectedInvitation : null}
          saving={saving}
          onClose={() => setModalMode(null)}
          onCreate={createInvitation}
          onUpdate={updateInvitation}
          onSend={sendInvitation}
          onCancel={cancelInvitation}
        />
      )}
    </>
  );
}

function PageHeader({ title, description, action }) {
  return (
    <section className="page-header">
      <div>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {action}
    </section>
  );
}

function Card({ children }) {
  return <section className="card">{children}</section>;
}

function Button({ children, variant = 'secondary', className = '', ...props }) {
  return (
    <button className={`button ${variant}-button ${className}`} type="button" {...props}>
      {children}
    </button>
  );
}

function Message({ type, children }) {
  return <div className={`message ${type}`} role="status">{children}</div>;
}

function InvitationsTable({ invitations, loading, selectedInvitationId, onSelect, onOpen }) {
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

function StatusBadge({ status }) {
  return <span className={`status-badge status-${statusClass(status)}`}>{formatStatus(status)}</span>;
}

function InvitationDetailsModal({
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
              <>
                <Button variant="secondary" onClick={sendDraftInvitation} disabled={saving}>
                  Send Invitation
                </Button>
              </>
            )}
            {canCancel && !isCreate && (
              <>
                <Button variant="danger" onClick={() => onCancel(invitation.id)} disabled={saving}>
                  Cancel Invitation
                </Button>
              </>
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

async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(options.headers || {})
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

async function readErrorMessage(response) {
  try {
    const payload = await response.json();
    return payload.message || `Request failed with status ${response.status}`;
  } catch {
    return `Request failed with status ${response.status}`;
  }
}

function formatDate(value) {
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

function formatStatus(status) {
  return (status || '').replaceAll('_', ' ');
}

function statusClass(status) {
  return (status || '').toLowerCase().replaceAll('_', '-');
}

function formatClientType(clientType) {
  if (clientType === 'INDIVIDUAL') {
    return 'Individual';
  }
  if (clientType === 'COMPANY') {
    return 'Company';
  }
  return clientType || '';
}

createRoot(document.getElementById('root')).render(<App />);
