import unfairAdvantageLogo from '../../assets/unfair-advantage-logo.webp';

export default function AppHeader() {
  return (
    <header className="app-header">
      <div className="brand">
        <img className="brand-logo" src={unfairAdvantageLogo} alt="" aria-hidden="true" />
        <div>
          <div className="brand-name">Accounting Client Onboarding Portal</div>
          <div className="brand-context">Internal onboarding workspace</div>
        </div>
      </div>
    </header>
  );
}
