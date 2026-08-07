import { useEffect, useMemo, useRef, useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Card from '../../../components/common/Card.jsx';
import Message from '../../../components/common/Message.jsx';
import ConfirmationDialog from '../../../components/dialogs/ConfirmationDialog.jsx';
import PageHeader from '../../../components/layout/PageHeader.jsx';
import { formatDate } from '../../../utils/formatters.js';
import {
  normalizeStatuses,
  parseStatusQuery,
  pushStatusQuery,
  replaceStatusQuery
} from '../../../utils/statusQueryParams.js';
import StatusBadge from '../invitations/StatusBadge.jsx';
import QuestionnaireDetailsModal from './QuestionnaireDetailsModal.jsx';
import {
  approveQuestionnaire,
  getQuestionnaire,
  getQuestionnaires,
  reopenQuestionnaire,
  sendQuestionnaireToXpm,
  updateQuestionnaire
} from './questionnaireApi.js';

const QUESTIONNAIRE_INVITATION_STATUSES = [
  'SUBMITTED',
  'APPROVED',
  'XPM_SENT'
];

export default function QuestionnairesPage({ locationKey = 0 }) {
  const filtersRef = useRef(null);

  const [questionnaires, setQuestionnaires] = useState([]);
  const [selectedQuestionnaireId, setSelectedQuestionnaireId] = useState(null);
  const [selectedDetails, setSelectedDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);
  const [pendingAction, setPendingAction] = useState(null);
  const [appliedStatuses, setAppliedStatuses] = useState(() =>
    getStatusesFromUrl()
  );
  const [draftStatuses, setDraftStatuses] = useState(() =>
    getStatusesFromUrl()
  );
  const [isFiltersOpen, setIsFiltersOpen] = useState(false);

  const selectedQuestionnaire = useMemo(
    () => questionnaires.find((questionnaire) => questionnaire.questionnaireId === selectedQuestionnaireId) || null,
    [questionnaires, selectedQuestionnaireId]
  );

  useEffect(() => {
    const nextStatuses = getStatusesFromUrl();

    replaceStatusQuery('/questionnaires', window.location.search, nextStatuses);
    setAppliedStatuses(nextStatuses);
    setDraftStatuses(nextStatuses);
    setIsFiltersOpen(false);
    setSelectedQuestionnaireId(null);
    loadQuestionnaires(nextStatuses, null);
  }, [locationKey]);

  async function loadQuestionnaires(
    invitationStatuses = [],
    preferredSelectedId = selectedQuestionnaireId
  ) {
    setLoading(true);
    setErrorMessage(null);
    setActionMessage(null);
    try {
      const data = await getQuestionnaires(invitationStatuses);
      const nextQuestionnaires = Array.isArray(data) ? data : [];
      setQuestionnaires(nextQuestionnaires);
      setSelectedQuestionnaireId(() => {
        if (!preferredSelectedId) {
          return null;
        }
        return nextQuestionnaires.some((questionnaire) => questionnaire.questionnaireId === preferredSelectedId)
          ? preferredSelectedId
          : null;
      });
    } catch (error) {
      setQuestionnaires([]);
      setSelectedQuestionnaireId(null);
      setErrorMessage(error.message);
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
    const nextStatuses = normalizeStatuses(
      draftStatuses,
      QUESTIONNAIRE_INVITATION_STATUSES
    );

    pushStatusQuery('/questionnaires', window.location.search, nextStatuses);
    setAppliedStatuses(nextStatuses);
    setIsFiltersOpen(false);
    setSelectedQuestionnaireId(null);

    await loadQuestionnaires(nextStatuses, null);
  }

  function clearFilters() {
    setDraftStatuses([]);
  }

  async function removeAppliedStatus(status) {
    const nextStatuses = normalizeStatuses(
      appliedStatuses.filter((appliedStatus) => appliedStatus !== status),
      QUESTIONNAIRE_INVITATION_STATUSES
    );

    pushStatusQuery('/questionnaires', window.location.search, nextStatuses);
    setAppliedStatuses(nextStatuses);
    setDraftStatuses(nextStatuses);
    setSelectedQuestionnaireId(null);

    await loadQuestionnaires(nextStatuses, null);
  }

  async function openQuestionnaire(questionnaire = selectedQuestionnaire) {
    if (!questionnaire) {
      return;
    }

    setSelectedQuestionnaireId(questionnaire.questionnaireId);
    setDetailsLoading(true);
    setErrorMessage(null);
    setActionMessage(null);
    try {
      setSelectedDetails(await getQuestionnaire(questionnaire.questionnaireId));
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setDetailsLoading(false);
    }
  }

  function requestApprove(questionnaire) {
    setPendingAction({ type: 'approve', questionnaireId: questionnaire.questionnaireId });
  }

  function requestReopen(questionnaire) {
    setPendingAction({ type: 'cancelApproval', questionnaireId: questionnaire.questionnaireId });
  }

  async function confirmPendingAction() {
    if (!pendingAction) {
      return;
    }

    setDetailsLoading(true);
    setErrorMessage(null);
    setActionMessage(null);
    try {
      const updated = pendingAction.type === 'approve'
        ? await approveQuestionnaire(pendingAction.questionnaireId)
        : await reopenQuestionnaire(pendingAction.questionnaireId);
      setSelectedDetails(updated);
      setPendingAction(null);
      await loadQuestionnaires(appliedStatuses, updated.questionnaireId);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setDetailsLoading(false);
    }
  }

  async function handleSendToXpm(questionnaire) {
    setDetailsLoading(true);
    setErrorMessage(null);
    setActionMessage(null);
    try {
      await sendQuestionnaireToXpm(questionnaire.questionnaireId);
      await loadQuestionnaires(appliedStatuses, questionnaire.questionnaireId);
    } catch (error) {
      setActionMessage(error.message);
    } finally {
      setDetailsLoading(false);
    }
  }

  async function handleUpdateQuestionnaire(questionnaire, payload) {
    setDetailsLoading(true);
    setErrorMessage(null);
    setActionMessage(null);
    try {
      const updated = await updateQuestionnaire(questionnaire.questionnaireId, payload);
      setSelectedDetails(updated);
      await loadQuestionnaires(appliedStatuses, updated.questionnaireId);
      return true;
    } catch (error) {
      setActionMessage(error.message);
      return false;
    } finally {
      setDetailsLoading(false);
    }
  }

  return (
    <>
      <div className="list-page">
        <PageHeader
          title="Questionnaires"
          description="Review client onboarding questionnaires submitted for accountant review."
          action={
            <div className="page-actions">
              <Button
                variant="secondary"
                onClick={() => loadQuestionnaires(appliedStatuses)}
                disabled={loading || detailsLoading}
              >
                Refresh
              </Button>
              <Button
                variant="secondary"
                className="selected-action-button"
                onClick={() => openQuestionnaire()}
                disabled={!selectedQuestionnaire || loading || detailsLoading}
              >
                View Selected
              </Button>
            </div>
          }
        />

        {errorMessage && <Message type="error">{errorMessage}</Message>}
        {actionMessage && <Message type="error">{actionMessage}</Message>}

        <div className="invitation-filters">
          <span className="questionnaire-filters__label">Filters:</span>

          <div className="invitation-filters__control" ref={filtersRef}>
            <Button
              variant="secondary"
              className="questionnaire-filter-button"
              onClick={isFiltersOpen ? closeFilters : openFilters}
              disabled={loading || detailsLoading}
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
                  {QUESTIONNAIRE_INVITATION_STATUSES.map((status) => (
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
                      detailsLoading ||
                      draftStatuses.length === 0
                    }
                  >
                    Clear
                  </Button>

                  <Button
                    variant="primary"
                    onClick={applyFilters}
                    disabled={loading || detailsLoading}
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
                className="invitation-filter-chip questionnaire-filter-chip"
                onClick={() => removeAppliedStatus(status)}
                disabled={loading || detailsLoading}
                aria-label={`Remove ${status} filter`}
              >
                <span>{status}</span>
                <span aria-hidden="true">&times;</span>
              </button>
            ))}
          </div>
        </div>

        <Card className="list-page__content">
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Preferred Name</th>
                  <th>Client Name</th>
                  <th>Status</th>
                  <th>Email</th>
                  <th>Mobile Number</th>
                  <th>Suburb</th>
                  <th>State</th>
                  <th>Submitted</th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr className="empty-row">
                    <td colSpan="8" className="empty-cell">Loading questionnaires...</td>
                  </tr>
                )}
                {!loading && questionnaires.length === 0 && (
                  <tr className="empty-row">
                    <td colSpan="8" className="empty-cell">No questionnaires.</td>
                  </tr>
                )}
                {!loading && questionnaires.map((questionnaire) => {
                  const selected = questionnaire.questionnaireId === selectedQuestionnaireId;
                  return (
                    <tr
                      key={questionnaire.questionnaireId}
                      className={selected ? 'selected-row' : ''}
                      tabIndex="0"
                      aria-selected={selected}
                      onClick={() => setSelectedQuestionnaireId(questionnaire.questionnaireId)}
                      onDoubleClick={() => openQuestionnaire(questionnaire)}
                      onKeyDown={(event) => {
                        if (event.key === 'Enter') {
                          openQuestionnaire(questionnaire);
                        }
                      }}
                    >
                      <td>{questionnaire.preferredName || ''}</td>
                      <td>{formatClientName(questionnaire)}</td>
                      <td><StatusBadge status={questionnaire.invitationStatus} /></td>
                      <td>{questionnaire.email || ''}</td>
                      <td>{questionnaire.mobilePhone || ''}</td>
                      <td>{questionnaire.suburb || ''}</td>
                      <td>{questionnaire.state || ''}</td>
                      <td>{formatDate(questionnaire.submittedAt)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </Card>
      </div>

      {selectedDetails && (
        <QuestionnaireDetailsModal
          questionnaire={selectedDetails}
          busy={detailsLoading}
          actionMessage={actionMessage}
          onClose={() => {
            setSelectedDetails(null);
            setActionMessage(null);
          }}
          onApprove={requestApprove}
          onReopen={requestReopen}
          onSendToXpm={handleSendToXpm}
          onSave={handleUpdateQuestionnaire}
        />
      )}

      {pendingAction && (
        <ConfirmationDialog
          title={pendingAction.type === 'approve' ? 'Approve Questionnaire' : 'Cancel Approval'}
          message={pendingAction.type === 'approve'
            ? 'Are you sure you want to approve this questionnaire?'
            : 'This will return the questionnaire to Submitted status. Continue?'}
          confirmLabel={pendingAction.type === 'approve' ? 'Approve' : 'Cancel Approval'}
          confirming={detailsLoading}
          onClose={() => setPendingAction(null)}
          onConfirm={confirmPendingAction}
        />
      )}
    </>
  );
}

function formatClientName(questionnaire) {
  return [questionnaire.firstName, questionnaire.middleName, questionnaire.lastName]
    .filter(Boolean)
    .join(' ');
}

function getStatusesFromUrl() {
  return parseStatusQuery(
    window.location.search,
    QUESTIONNAIRE_INVITATION_STATUSES
  );
}
