import { useState } from 'react';
import AppLayout from './components/layout/AppLayout.jsx';
import ClientsPage from './features/admin/clients/ClientsPage.jsx';
import InvitationsPage from './features/admin/invitations/InvitationsPage.jsx';
import QuestionnairesPage from './features/admin/questionnaires/QuestionnairesPage.jsx';
import PublicOnboardingPage from './features/public/onboarding/PublicOnboardingPage.jsx';

export default function App() {
  const onboardingMatch = window.location.pathname.match(/^\/onboarding\/([^/]+)\/?$/);
  const initialAdminPage = getAdminPageFromPath(window.location.pathname);
  const [adminPage, setAdminPage] = useState(initialAdminPage);

  if (onboardingMatch) {
    return <PublicOnboardingPage token={decodeURIComponent(onboardingMatch[1])} />;
  }

  function navigateAdmin(page) {
    const nextPath = page === 'questionnaires' ? '/questionnaires' : page === 'clients' ? '/clients' : '/';
    window.history.pushState({}, '', nextPath);
    setAdminPage(page);
  }

  return (
    <AppLayout activePage={adminPage} onNavigate={navigateAdmin}>
      {adminPage === 'questionnaires' && <QuestionnairesPage />}
      {adminPage === 'clients' && <ClientsPage />}
      {adminPage === 'invitations' && <InvitationsPage />}
    </AppLayout>
  );
}

function getAdminPageFromPath(pathname) {
  if (pathname === '/questionnaires') {
    return 'questionnaires';
  }
  if (pathname === '/clients') {
    return 'clients';
  }
  return 'invitations';
}
