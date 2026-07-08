import { useEffect, useMemo, useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Card from '../../../components/common/Card.jsx';
import Message from '../../../components/common/Message.jsx';
import ConfirmationDialog from '../../../components/dialogs/ConfirmationDialog.jsx';
import PageHeader from '../../../components/layout/PageHeader.jsx';
import { formatDate } from '../../../utils/formatters.js';
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

export default function QuestionnairesPage() {
  const [questionnaires, setQuestionnaires] = useState([]);
  const [selectedQuestionnaireId, setSelectedQuestionnaireId] = useState(null);
  const [selectedDetails, setSelectedDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);
  const [pendingAction, setPendingAction] = useState(null);

  const selectedQuestionnaire = useMemo(
    () => questionnaires.find((questionnaire) => questionnaire.questionnaireId === selectedQuestionnaireId) || null,
    [questionnaires, selectedQuestionnaireId]
  );

  useEffect(() => {
    loadQuestionnaires();
  }, []);

  async function loadQuestionnaires(preferredSelectedId = selectedQuestionnaireId) {
    setLoading(true);
    setErrorMessage(null);
    setActionMessage(null);
    try {
      const data = await getQuestionnaires();
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
      await loadQuestionnaires(updated.questionnaireId);
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
      await loadQuestionnaires(updated.questionnaireId);
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
      <PageHeader
        title="Questionnaires"
        description="Review client onboarding questionnaires submitted for accountant review."
        action={
          <div className="page-actions">
            <Button variant="secondary" onClick={() => loadQuestionnaires()} disabled={loading || detailsLoading}>
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

      <Card>
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
                  <td colSpan="8" className="empty-cell">No submitted questionnaires yet.</td>
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
