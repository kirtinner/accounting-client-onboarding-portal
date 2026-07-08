import { useEffect } from 'react';
import Button from '../common/Button.jsx';

export default function ConfirmationDialog({ title, message, confirmLabel, confirming, onClose, onConfirm }) {
  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        onClose();
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <div className="modal-backdrop confirmation-backdrop" role="dialog" aria-modal="true" aria-labelledby="confirmationTitle">
      <div className="modal-panel confirmation-panel">
        <div className="modal-header">
          <h2 id="confirmationTitle">{title}</h2>
        </div>
        <div className="confirmation-body">
          <p>{message}</p>
          <div className="form-actions">
            <Button variant="secondary" onClick={onClose} disabled={confirming}>Cancel</Button>
            <Button variant="danger" onClick={onConfirm} disabled={confirming}>
              {confirming ? 'Cancelling...' : confirmLabel}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
