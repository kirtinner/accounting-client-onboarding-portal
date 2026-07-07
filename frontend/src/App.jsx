import AppLayout from './components/layout/AppLayout.jsx';
import InvitationsPage from './features/admin/invitations/InvitationsPage.jsx';
import PublicOnboardingPage from './features/public/onboarding/PublicOnboardingPage.jsx';

export default function App() {
  const onboardingMatch = window.location.pathname.match(/^\/onboarding\/([^/]+)\/?$/);

  if (onboardingMatch) {
    return <PublicOnboardingPage token={decodeURIComponent(onboardingMatch[1])} />;
  }

  return (
    <AppLayout>
      <InvitationsPage />
    </AppLayout>
  );
}
