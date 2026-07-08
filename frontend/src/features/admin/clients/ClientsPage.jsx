import Card from '../../../components/common/Card.jsx';
import PageHeader from '../../../components/layout/PageHeader.jsx';

export default function ClientsPage() {
  return (
    <>
      <PageHeader
        title="Clients"
        description="Clients successfully synchronized with Xero Practice Manager will appear here."
      />

      <Card>
        <div className="empty-state">
          No clients have been synchronized with Xero Practice Manager yet.
        </div>
      </Card>
    </>
  );
}
