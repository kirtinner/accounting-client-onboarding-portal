import React, { useEffect, useState } from 'react';
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
            <span className="nav-item disabled">Questionnaires</span>
            <span className="nav-item disabled">Documents</span>
            <span className="nav-item disabled">Xero</span>
          </nav>
        </aside>
        <main className="main-content">{children}</main>
      </div>
    </div>
  );
}

function InvitationsPage() {
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    loadInvitations();
  }, []);

  async function loadInvitations() {
    setLoading(true);
    try {
      const data = await apiRequest(API_BASE);
      setInvitations(Array.isArray(data) ? data : []);
    } catch (error) {
      setInvitations([]);
      setMessage({ type: 'error', text: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function createInvitation(payload) {
    try {
      await apiRequest(API_BASE, {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      setModalOpen(false);
      await loadInvitations();
      setMessage({ type: 'success', text: 'Invitation created.' });
    } catch (error) {
      setMessage({ type: 'error', text: error.message });
    }
  }

  async function sendInvitation(id) {
    try {
      await apiRequest(`${API_BASE}/${id}/send`, { method: 'POST' });
      await loadInvitations();
      setMessage({ type: 'success', text: 'Invitation marked as sent.' });
    } catch (error) {
      setMessage({ type: 'error', text: error.message });
    }
  }

  async function cancelInvitation(id) {
    if (!window.confirm('Cancel this invitation?')) {
      return;
    }

    try {
      await apiRequest(`${API_BASE}/${id}/cancel`, { method: 'POST' });
      await loadInvitations();
      setMessage({ type: 'success', text: 'Invitation cancelled.' });
    } catch (error) {
      setMessage({ type: 'error', text: error.message });
    }
  }

  return (
    <>
      <PageHeader
        title="Client Invitations"
        description="Manage onboarding invitations sent to new clients."
        action={<Button variant="primary" onClick={() => setModalOpen(true)}>New Invitation</Button>}
      />

      {message && <Message type={message.type}>{message.text}</Message>}

      <Card>
        <div className="card-header">
          <h2>Invitations</h2>
          <Button variant="secondary" onClick={loadInvitations}>Refresh</Button>
        </div>
        <InvitationsTable
          invitations={invitations}
          loading={loading}
          onSend={sendInvitation}
          onCancel={cancelInvitation}
        />
      </Card>

      {modalOpen && (
        <InvitationModal
          onClose={() => setModalOpen(false)}
          onSubmit={createInvitation}
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

function InvitationsTable({ invitations, loading, onSend, onCancel }) {
  let body;

  if (loading) {
    body = (
      <tr>
        <td colSpan="7" className="empty-cell">Loading invitations...</td>
      </tr>
    );
  } else if (invitations.length === 0) {
    body = (
      <tr>
        <td colSpan="7" className="empty-cell">No invitations yet. Create your first invitation.</td>
      </tr>
    );
  } else {
    body = invitations.map((invitation) => (
      <tr key={invitation.id}>
        <td>{invitation.preferredName || ''}</td>
        <td>{invitation.email || ''}</td>
        <td>{formatClientType(invitation.clientType)}</td>
        <td><StatusBadge status={invitation.status} /></td>
        <td>{formatDate(invitation.createdAt)}</td>
        <td>{formatDate(invitation.expiresAt)}</td>
        <td>
          <RowActions invitation={invitation} onSend={onSend} onCancel={onCancel} />
        </td>
      </tr>
    ));
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
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>{body}</tbody>
      </table>
    </div>
  );
}

function RowActions({ invitation, onSend, onCancel }) {
  if (invitation.status === 'DRAFT') {
    return (
      <div className="actions">
        <Button variant="secondary" onClick={() => onSend(invitation.id)}>Send</Button>
        <Button variant="danger" onClick={() => onCancel(invitation.id)}>Cancel</Button>
      </div>
    );
  }

  if (invitation.status === 'SENT') {
    return (
      <div className="actions">
        <Button variant="danger" onClick={() => onCancel(invitation.id)}>Cancel</Button>
      </div>
    );
  }

  return <span className="no-actions" aria-label="No actions available" />;
}

function StatusBadge({ status }) {
  return <span className={`status-badge status-${statusClass(status)}`}>{formatStatus(status)}</span>;
}

function InvitationModal({ onClose, onSubmit }) {
  const [form, setForm] = useState({
    preferredName: '',
    email: '',
    clientType: 'INDIVIDUAL'
  });

  function updateField(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value
    }));
  }

  function submit(event) {
    event.preventDefault();
    onSubmit({
      preferredName: form.preferredName.trim(),
      email: form.email.trim(),
      clientType: form.clientType
    });
  }

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
      <div className="modal-panel">
        <div className="modal-header">
          <h2 id="modalTitle">New Invitation</h2>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close">x</button>
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
              autoFocus
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
            />
          </label>
          <label className="field">
            <span>Client Type</span>
            <select name="clientType" value={form.clientType} onChange={updateField} required>
              <option value="INDIVIDUAL">Individual</option>
              <option value="COMPANY">Company</option>
            </select>
          </label>
          <div className="form-actions">
            <Button variant="secondary" onClick={onClose}>Cancel</Button>
            <button className="button primary-button" type="submit">Create Invitation</button>
          </div>
        </form>
      </div>
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
