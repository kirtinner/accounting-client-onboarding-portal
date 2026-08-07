import { useEffect, useRef, useState } from 'react';
import AppLayout from './components/layout/AppLayout.jsx';
import AuditLogPage from './features/admin/audit/AuditLogPage.jsx';
import ClientsPage from './features/admin/clients/ClientsPage.jsx';
import InvitationsPage from './features/admin/invitations/InvitationsPage.jsx';
import QuestionnairesPage from './features/admin/questionnaires/QuestionnairesPage.jsx';
import PublicOnboardingPage from './features/public/onboarding/PublicOnboardingPage.jsx';

export default function App() {
  const onboardingMatch = window.location.pathname.match(/^\/onboarding\/([^/]+)\/?$/);
  const initialAdminPage = getAdminPageFromPath(window.location.pathname);
  const pageSearchesRef = useRef({
    invitations: initialAdminPage === 'invitations' ? window.location.search : '',
    questionnaires: initialAdminPage === 'questionnaires' ? window.location.search : '',
    clients: initialAdminPage === 'clients' ? window.location.search : '',
    auditLog: initialAdminPage === 'auditLog' ? window.location.search : ''
  });
  const [adminPage, setAdminPage] = useState(initialAdminPage);
  const [locationKey, setLocationKey] = useState(0);

  useEffect(() => {
    function handlePopState() {
      const nextPage = getAdminPageFromPath(window.location.pathname);
      pageSearchesRef.current[nextPage] = window.location.search;
      setAdminPage(nextPage);
      setLocationKey((currentKey) => currentKey + 1);
    }

    window.addEventListener('popstate', handlePopState);

    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, []);

  if (onboardingMatch) {
    return <PublicOnboardingPage token={decodeURIComponent(onboardingMatch[1])} />;
  }

  function navigateAdmin(page) {
    pageSearchesRef.current[adminPage] = window.location.search;

    const nextPath = getAdminPath(page);
    const nextSearch = pageSearchesRef.current[page] || '';

    window.history.pushState({}, '', `${nextPath}${nextSearch}`);
    setAdminPage(page);
    setLocationKey((currentKey) => currentKey + 1);
  }

  return (
    <AppLayout activePage={adminPage} onNavigate={navigateAdmin}>
      {adminPage === 'questionnaires' && (
        <QuestionnairesPage locationKey={locationKey} />
      )}
      {adminPage === 'clients' && <ClientsPage />}
      {adminPage === 'auditLog' && <AuditLogPage />}
      {adminPage === 'invitations' && (
        <InvitationsPage locationKey={locationKey} />
      )}
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
  if (pathname === '/audit-log') {
    return 'auditLog';
  }
  return 'invitations';
}

function getAdminPath(page) {
  if (page === 'questionnaires') {
    return '/questionnaires';
  }
  if (page === 'clients') {
    return '/clients';
  }
  if (page === 'auditLog') {
    return '/audit-log';
  }
  return '/invitations';
}
