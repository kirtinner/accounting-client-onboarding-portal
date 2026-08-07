import { useEffect, useState } from 'react';
import Card from '../../../components/common/Card.jsx';
import Message from '../../../components/common/Message.jsx';
import PageHeader from '../../../components/layout/PageHeader.jsx';
import { formatDate } from '../../../utils/formatters.js';
import { getAuditLogs } from './auditLogApi.js';

export default function AuditLogPage() {
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState(null);

  useEffect(() => {
    loadAuditLogs();
  }, []);

  async function loadAuditLogs() {
    setLoading(true);
    setErrorMessage(null);

    try {
      const data = await getAuditLogs();
      setAuditLogs(Array.isArray(data) ? data : []);
    } catch (error) {
      setAuditLogs([]);
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="list-page">
      <PageHeader
        title="Audit Log"
        description="Review recorded administration and workflow events."
      />

      {errorMessage && <Message type="error">{errorMessage}</Message>}

      <Card className="list-page__content">
        <div className="table-wrap">
          <table className="data-table audit-log-table">
            <thead>
              <tr>
                <th>Date &amp; Time</th>
                <th>Actor</th>
                <th>Action</th>
                <th>Entity</th>
                <th>Result</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              {loading && (
                <tr className="empty-row">
                  <td colSpan="6" className="empty-cell">Loading audit log...</td>
                </tr>
              )}
              {!loading && auditLogs.length === 0 && (
                <tr className="empty-row">
                  <td colSpan="6" className="empty-cell">No audit log records.</td>
                </tr>
              )}
              {!loading && auditLogs.map((auditLog) => (
                <tr key={auditLog.id}>
                  <td>{formatDate(auditLog.occurredAt)}</td>
                  <td>{displayValue(auditLog.actor)}</td>
                  <td>{displayValue(auditLog.action)}</td>
                  <td>{formatEntity(auditLog)}</td>
                  <td>{displayValue(auditLog.result)}</td>
                  <td>{displayValue(auditLog.description)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}

function formatEntity(auditLog) {
  if (!auditLog?.entityType || !auditLog?.entityId) {
    return '-';
  }

  return `${auditLog.entityType} #${auditLog.entityId}`;
}

function displayValue(value) {
  if (value === null || value === undefined || value === '') {
    return '-';
  }

  return value;
}
