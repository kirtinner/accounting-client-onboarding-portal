import Card from '../../../components/common/Card.jsx';
import PageHeader from '../../../components/layout/PageHeader.jsx';

export default function ClientsPage() {
  return (
    <div className="list-page">
      <PageHeader
        title="Clients"
        description="Clients successfully synchronized with Xero Practice Manager will appear here."
      />

      <Card className="list-page__content list-page__content--empty">
        <div className="empty-state">
          No clients have been synchronized with Xero Practice Manager yet.
        </div>
      </Card>
    </div>
  );
}
