import { useEffect, useMemo, useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Card from '../../../components/common/Card.jsx';
import Message from '../../../components/common/Message.jsx';
import ConfirmationDialog from '../../../components/dialogs/ConfirmationDialog.jsx';
import PageHeader from '../../../components/layout/PageHeader.jsx';
import InvitationDetailsModal from './InvitationDetailsModal.jsx';
import InvitationsTable from './InvitationsTable.jsx';
import {
  cancelInvitation,
  createInvitation,
  getInvitations,
  sendInvitation,
  updateInvitation
} from './invitationApi.js';

export default function InvitationsPage() {
  const [invitations, setInvitations] = useState([]);
  const [selectedInvitationId, setSelectedInvitationId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalMode, setModalMode] = useState(null);
  const [modalInvitation, setModalInvitation] = useState(null);
  const [pendingCancelInvitationId, setPendingCancelInvitationId] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);

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
      const data = await getInvitations();
      const nextInvitations = Array.isArray(data) ? data : [];
      setInvitations(nextInvitations);
      setSelectedInvitationId(() => {
        if (!preferredSelectedId) {
          return null;
        }
        return nextInvitations.some((invitation) => invitation.id === preferredSelectedId) ? preferredSelectedId : null;
      });
      setModalInvitation((current) => {
        if (!current?.id) {
          return current;
        }
        return nextInvitations.find((invitation) => invitation.id === current.id) || current;
      });
      return nextInvitations;
    } catch (error) {
      setInvitations([]);
      setSelectedInvitationId(null);
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setErrorMessage(null);
    setModalInvitation(null);
    setModalMode('create');
  }

  function openSelectedInvitation(invitation = selectedInvitation) {
    if (!invitation) {
      return;
    }

    setSelectedInvitationId(invitation.id);
    setErrorMessage(null);
    setModalInvitation(invitation);
    setModalMode('details');
  }

  async function handleCreateInvitation(payload) {
    setSaving(true);
    try {
      const createdInvitation = await createInvitation(payload);
      setSelectedInvitationId(createdInvitation?.id || null);
      setModalInvitation(createdInvitation);
      setModalMode('details');
      await loadInvitations(createdInvitation?.id || null);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleUpdateInvitation(id, payload) {
    setSaving(true);
    try {
      const updatedInvitation = await updateInvitation(id, payload);
      setModalInvitation(updatedInvitation);
      await loadInvitations(id);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleSendInvitation(id, payload = null) {
    setSaving(true);
    try {
      await sendInvitation(id, payload);
      setModalMode(null);
      setModalInvitation(null);
      await loadInvitations(id);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  async function confirmCancelInvitation() {
    if (!pendingCancelInvitationId) {
      return;
    }

    const id = pendingCancelInvitationId;
    setSaving(true);
    try {
      await cancelInvitation(id);
      setPendingCancelInvitationId(null);
      setModalMode(null);
      setModalInvitation(null);
      await loadInvitations(id);
    } catch (error) {
      setErrorMessage(error.message);
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
            <Button
              variant="secondary"
              className="selected-action-button"
              onClick={() => openSelectedInvitation()}
              disabled={!selectedInvitation || loading || saving}
            >
              {selectedButtonText}
            </Button>
            <Button variant="primary" className="new-invitation-button" onClick={openCreateModal} disabled={saving}>
              New Invitation
            </Button>
          </div>
        }
      />

      {errorMessage && <Message type="error">{errorMessage}</Message>}

      <Card>
        <InvitationsTable
          invitations={invitations}
          loading={loading}
          selectedInvitationId={selectedInvitationId}
          onSelect={setSelectedInvitationId}
          onOpen={openSelectedInvitation}
        />
      </Card>

      {modalMode && (modalMode === 'create' || modalInvitation) && (
        <InvitationDetailsModal
          mode={modalMode}
          invitation={modalMode === 'details' ? modalInvitation : null}
          saving={saving}
          onClose={() => {
            setModalMode(null);
            setModalInvitation(null);
          }}
          onCreate={handleCreateInvitation}
          onUpdate={handleUpdateInvitation}
          onSend={handleSendInvitation}
          onCancel={setPendingCancelInvitationId}
        />
      )}

      {pendingCancelInvitationId && (
        <ConfirmationDialog
          title="Cancel Invitation"
          message="Are you sure you want to cancel this invitation?"
          confirmLabel="Yes, Cancel Invitation"
          confirming={saving}
          onClose={() => setPendingCancelInvitationId(null)}
          onConfirm={confirmCancelInvitation}
        />
      )}
    </>
  );
}
