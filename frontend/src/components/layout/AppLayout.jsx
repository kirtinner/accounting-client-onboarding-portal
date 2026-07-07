import unfairAdvantageLogo from '../../assets/unfair-advantage-logo.webp';

export default function AppLayout({ children }) {
  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">
          <img className="brand-logo" src={unfairAdvantageLogo} alt="" aria-hidden="true" />
          <div>
            <div className="brand-name">Accounting Client Onboarding Portal</div>
            <div className="brand-context">Internal onboarding workspace</div>
          </div>
        </div>
      </header>
      <div className="app-body">
        <aside className="sidebar" aria-label="Primary navigation">
          <nav className="nav-list">
            <a className="nav-item active" href="/">Client Invitations</a>
            <span className="nav-item disabled" aria-disabled="true">Questionnaires</span>
            <span className="nav-item disabled" aria-disabled="true">Documents</span>
            <span className="nav-item disabled" aria-disabled="true">Xero</span>
          </nav>
        </aside>
        <main className="main-content">{children}</main>
      </div>
    </div>
  );
}
