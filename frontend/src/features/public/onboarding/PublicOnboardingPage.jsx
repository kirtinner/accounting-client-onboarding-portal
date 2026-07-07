import { useEffect, useState } from 'react';
import Message from '../../../components/common/Message.jsx';
import { formatClientType, formatDate } from '../../../utils/formatters.js';
import { getPublicOnboarding } from './publicOnboardingApi.js';

export default function PublicOnboardingPage({ token }) {
  const [onboarding, setOnboarding] = useState(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState(null);

  useEffect(() => {
    async function loadOnboarding() {
      setLoading(true);
      setErrorMessage(null);
      try {
        setOnboarding(await getPublicOnboarding(token));
      } catch (error) {
        setOnboarding(null);
        setErrorMessage(error.message || 'This onboarding invitation is not available.');
      } finally {
        setLoading(false);
      }
    }

    loadOnboarding();
  }, [token]);

  return (
    <main className="public-shell">
      <section className="public-panel">
        <div className="public-header">
          <p className="public-eyebrow">Unfair Advantage Accounting</p>
          <h1>Client Onboarding</h1>
        </div>

        {loading && <div className="public-loading">Loading onboarding invitation...</div>}

        {!loading && errorMessage && (
          <Message type="error">
            {errorMessage}
          </Message>
        )}

        {!loading && onboarding && (
          <div className="public-content">
            <p className="public-greeting">Hello {onboarding.preferredName},</p>
            <p className="public-copy">The onboarding questionnaire will be available here.</p>

            <div className="public-details">
              <Detail label="Invitation Email" value={onboarding.email} />
              <Detail label="Client Type" value={formatClientType(onboarding.clientType)} />
              <Detail label="Expires" value={formatDate(onboarding.expiresAt)} />
            </div>
          </div>
        )}
      </section>
    </main>
  );
}

function Detail({ label, value }) {
  return (
    <div className="public-detail">
      <span>{label}</span>
      <strong>{value || '-'}</strong>
    </div>
  );
}
