import { useEffect, useMemo, useRef, useState } from 'react';
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

const INVITATION_STATUSES = [
    'DRAFT',
    'SENT',
    'SUBMITTED',
    'APPROVED',
    'XPM_SENT',
    'EXPIRED',
    'CANCELLED'
];

export default function InvitationsPage() {
    const filtersRef = useRef(null);

    const [invitations, setInvitations] = useState([]);
    const [selectedInvitationId, setSelectedInvitationId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [modalMode, setModalMode] = useState(null);
    const [modalInvitation, setModalInvitation] = useState(null);
    const [pendingCancelInvitationId, setPendingCancelInvitationId] = useState(null);
    const [errorMessage, setErrorMessage] = useState(null);

    const [appliedStatuses, setAppliedStatuses] = useState([]);
    const [draftStatuses, setDraftStatuses] = useState([]);
    const [isFiltersOpen, setIsFiltersOpen] = useState(false);

    const selectedInvitation = useMemo(
        () =>
            invitations.find(
                (invitation) => invitation.id === selectedInvitationId
            ) || null,
        [invitations, selectedInvitationId]
    );

    const selectedButtonText =
        selectedInvitation?.status === 'DRAFT'
            ? 'Edit Selected'
            : 'View Selected';

    useEffect(() => {
        loadInvitations([]);
    }, []);

    async function loadInvitations(
        statuses = [],
        preferredSelectedId = selectedInvitationId
    ) {
        setLoading(true);
        setErrorMessage(null);

        try {
            const data = await getInvitations(statuses);
            const nextInvitations = Array.isArray(data) ? data : [];

            setInvitations(nextInvitations);

            setSelectedInvitationId(() => {
                if (!preferredSelectedId) {
                    return null;
                }

                const selectedStillExists = nextInvitations.some(
                    (invitation) => invitation.id === preferredSelectedId
                );

                return selectedStillExists ? preferredSelectedId : null;
            });

            setModalInvitation((current) => {
                if (!current?.id) {
                    return current;
                }

                return (
                    nextInvitations.find(
                        (invitation) => invitation.id === current.id
                    ) || current
                );
            });

            return nextInvitations;
        } catch (error) {
            setInvitations([]);
            setSelectedInvitationId(null);
            setErrorMessage(error.message);
            return [];
        } finally {
            setLoading(false);
        }
    }

    function openFilters() {
        setDraftStatuses([...appliedStatuses]);
        setIsFiltersOpen(true);
    }

    function closeFilters() {
        setDraftStatuses([...appliedStatuses]);
        setIsFiltersOpen(false);
    }

    useEffect(() => {
        if (!isFiltersOpen) {
            return undefined;
        }

        function handleMouseDown(event) {
            if (
                filtersRef.current &&
                !filtersRef.current.contains(event.target)
            ) {
                closeFilters();
            }
        }

        function handleKeyDown(event) {
            if (event.key === 'Escape') {
                closeFilters();
            }
        }

        document.addEventListener('mousedown', handleMouseDown);
        document.addEventListener('keydown', handleKeyDown);

        return () => {
            document.removeEventListener('mousedown', handleMouseDown);
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, [isFiltersOpen, appliedStatuses]);

    function toggleDraftStatus(status) {
        setDraftStatuses((currentStatuses) => {
            if (currentStatuses.includes(status)) {
                return currentStatuses.filter(
                    (currentStatus) => currentStatus !== status
                );
            }

            return [...currentStatuses, status];
        });
    }

    async function applyFilters() {
        const nextStatuses = [...draftStatuses];

        setAppliedStatuses(nextStatuses);
        setIsFiltersOpen(false);
        setSelectedInvitationId(null);

        await loadInvitations(nextStatuses, null);
    }

    function clearFilters() {
        setDraftStatuses([]);
    }

    async function removeAppliedStatus(status) {
        const nextStatuses = appliedStatuses.filter(
            (appliedStatus) => appliedStatus !== status
        );

        setAppliedStatuses(nextStatuses);
        setDraftStatuses(nextStatuses);
        setSelectedInvitationId(null);

        await loadInvitations(nextStatuses, null);
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
        setErrorMessage(null);

        try {
            const createdInvitation = await createInvitation(payload);

            setSelectedInvitationId(createdInvitation?.id || null);
            setModalInvitation(createdInvitation);
            setModalMode('details');

            await loadInvitations(
                appliedStatuses,
                createdInvitation?.id || null
            );
        } catch (error) {
            setErrorMessage(error.message);
        } finally {
            setSaving(false);
        }
    }

    async function handleUpdateInvitation(id, payload) {
        setSaving(true);
        setErrorMessage(null);

        try {
            const updatedInvitation = await updateInvitation(id, payload);

            setModalInvitation(updatedInvitation);

            await loadInvitations(appliedStatuses, id);
        } catch (error) {
            setErrorMessage(error.message);
        } finally {
            setSaving(false);
        }
    }

    async function handleSendInvitation(id, payload = null) {
        setSaving(true);
        setErrorMessage(null);

        try {
            await sendInvitation(id, payload);

            setModalMode(null);
            setModalInvitation(null);

            await loadInvitations(appliedStatuses, id);
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
        setErrorMessage(null);

        try {
            await cancelInvitation(id);

            setPendingCancelInvitationId(null);
            setModalMode(null);
            setModalInvitation(null);

            await loadInvitations(appliedStatuses, id);
        } catch (error) {
            setErrorMessage(error.message);
        } finally {
            setSaving(false);
        }
    }

    return (
        <>
            <PageHeader
                title="Invitations"
                description="Manage onboarding invitations sent to new clients."
                action={
                    <div className="page-actions">
                        <Button
                            variant="secondary"
                            onClick={() => loadInvitations(appliedStatuses)}
                            disabled={loading || saving}
                        >
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

                        <Button
                            variant="primary"
                            className="new-invitation-button"
                            onClick={openCreateModal}
                            disabled={saving}
                        >
                            New Invitation
                        </Button>
                    </div>
                }
            />

            {errorMessage && (
                <Message type="error">{errorMessage}</Message>
            )}

            <div className="invitation-filters">
                <div className="invitation-filters__control" ref={filtersRef}>
                    <Button
                        variant="secondary"
                        onClick={isFiltersOpen ? closeFilters : openFilters}
                        disabled={loading || saving}
                    >
                        {appliedStatuses.length > 0
                            ? `Filters (${appliedStatuses.length})`
                            : 'Filters'}
                    </Button>

                    {isFiltersOpen && (
                        <div className="invitation-filters-popover">
                            <div className="invitation-filters-popover__header">
                                <div className="invitation-filters-popover__title">
                                    Invitation status
                                </div>

                                <button
                                    type="button"
                                    className="icon-button invitation-filters-popover__close"
                                    onClick={closeFilters}
                                    aria-label="Close filters"
                                >
                                    &times;
                                </button>
                            </div>

                            <div className="invitation-filters-popover__options">
                                {INVITATION_STATUSES.map((status) => (
                                    <label
                                        key={status}
                                        className="invitation-filter-option"
                                    >
                                        <input
                                            type="checkbox"
                                            checked={draftStatuses.includes(status)}
                                            onChange={() => toggleDraftStatus(status)}
                                        />

                                        <span>{status}</span>
                                    </label>
                                ))}
                            </div>

                            <div className="invitation-filters-popover__actions">
                                <Button
                                    variant="secondary"
                                    onClick={clearFilters}
                                    disabled={
                                        loading ||
                                        saving ||
                                        draftStatuses.length === 0
                                    }
                                >
                                    Clear
                                </Button>

                                <Button
                                    variant="primary"
                                    onClick={applyFilters}
                                    disabled={loading || saving}
                                >
                                    Apply
                                </Button>
                            </div>
                        </div>
                    )}
                </div>

                <div className="invitation-filter-chips">
                    {appliedStatuses.map((status) => (
                        <button
                            key={status}
                            type="button"
                            className="invitation-filter-chip"
                            onClick={() => removeAppliedStatus(status)}
                            disabled={loading || saving}
                            aria-label={`Remove ${status} filter`}
                        >
                            <span>{status}</span>
                            <span aria-hidden="true">&times;</span>
                        </button>
                    ))}
                </div>
            </div>

            <Card>
                <InvitationsTable
                    invitations={invitations}
                    loading={loading}
                    selectedInvitationId={selectedInvitationId}
                    onSelect={setSelectedInvitationId}
                    onOpen={openSelectedInvitation}
                />
            </Card>

            {modalMode &&
                (modalMode === 'create' || modalInvitation) && (
                    <InvitationDetailsModal
                        mode={modalMode}
                        invitation={
                            modalMode === 'details' ? modalInvitation : null
                        }
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
