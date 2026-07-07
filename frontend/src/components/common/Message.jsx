export default function Message({ type, children }) {
  return <div className={`message ${type}`} role="status">{children}</div>;
}
