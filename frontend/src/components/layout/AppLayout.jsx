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
            <div className="nav-section" aria-label="Main">
              <div className="nav-section-heading">MAIN</div>
              <a
                className={`nav-item ${activePage === 'invitations' ? 'active' : ''}`}
                href="/invitations"
                onClick={navigate('invitations')}
              >
                Invitations
              </a>
              <a
                className={`nav-item ${activePage === 'questionnaires' ? 'active' : ''}`}
                href="/questionnaires"
                onClick={navigate('questionnaires')}
              >
                Questionnaires
              </a>
              <a
                className={`nav-item ${activePage === 'clients' ? 'active' : ''}`}
                href="/clients"
                onClick={navigate('clients')}
              >
                Clients
              </a>
            </div>

            <div className="nav-section" aria-label="Administration">
              <div className="nav-section-heading">ADMINISTRATION</div>
              <a
                className={`nav-item ${activePage === 'auditLog' ? 'active' : ''}`}
                href="/audit-log"
                onClick={navigate('auditLog')}
              >
                Audit Log
              </a>
            </div>
          </nav>
        </aside>
        <main className="main-content">{children}</main>
      </div>
    </div>
  );
}
