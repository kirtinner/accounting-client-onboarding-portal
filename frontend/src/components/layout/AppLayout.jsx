import AppHeader from './AppHeader.jsx';

export default function AppLayout({ children, activePage = 'invitations', onNavigate }) {
  function navigate(page) {
    return (event) => {
      event.preventDefault();
      onNavigate?.(page);
    };
  }

  return (
    <div className="app-shell">
      <AppHeader />
      <div className="app-body">
        <aside className="sidebar" aria-label="Primary navigation">
          <nav className="nav-list">
            <a
              className={`nav-item ${activePage === 'invitations' ? 'active' : ''}`}
              href="/"
              onClick={navigate('invitations')}
            >
              Client Invitations
            </a>
            <a
              className={`nav-item ${activePage === 'questionnaires' ? 'active' : ''}`}
              href="/questionnaires"
              onClick={navigate('questionnaires')}
            >
              Questionnaires
            </a>
            <span className="nav-item disabled" aria-disabled="true">Documents</span>
            <span className="nav-item disabled" aria-disabled="true">Xero</span>
          </nav>
        </aside>
        <main className="main-content">{children}</main>
      </div>
    </div>
  );
}
