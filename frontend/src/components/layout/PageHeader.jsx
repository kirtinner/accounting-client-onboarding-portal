export default function PageHeader({ title, description, action }) {
  return (
    <section className="page-header">
      <div>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {action}
    </section>
  );
}
