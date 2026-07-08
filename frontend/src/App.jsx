import { useState } from 'react';
import AppLayout from './components/layout/AppLayout.jsx';
import InvitationsPage from './features/admin/invitations/InvitationsPage.jsx';
import QuestionnairesPage from './features/admin/questionnaires/QuestionnairesPage.jsx';
import PublicOnboardingPage from './features/public/onboarding/PublicOnboardingPage.jsx';

export default function App() {
  const onboardingMatch = window.location.pathname.match(/^\/onboarding\/([^/]+)\/?$/);
  const initialAdminPage = window.location.pathname === '/questionnaires' ? 'questionnaires' : 'invitations';
  const [adminPage, setAdminPage] = useState(initialAdminPage);

  if (onboardingMatch) {
    return <PublicOnboardingPage token={decodeURIComponent(onboardingMatch[1])} />;
  }

  function navigateAdmin(page) {
    const nextPath = page === 'questionnaires' ? '/questionnaires' : '/';
    window.history.pushState({}, '', nextPath);
    setAdminPage(page);
  }

  return (
    <AppLayout activePage={adminPage} onNavigate={navigateAdmin}>
      {adminPage === 'questionnaires' ? <QuestionnairesPage /> : <InvitationsPage />}
    </AppLayout>
  );
}
