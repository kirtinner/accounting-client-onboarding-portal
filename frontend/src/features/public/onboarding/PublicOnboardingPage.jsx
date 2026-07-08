import { useEffect, useMemo, useRef, useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Message from '../../../components/common/Message.jsx';
import AppHeader from '../../../components/layout/AppHeader.jsx';
import { lookupAustralianPostcode, AUSTRALIAN_STATES } from '../../../utils/australianPostcodes.js';
import { formatDate } from '../../../utils/formatters.js';
import {
  getPublicOnboarding,
  savePublicQuestionnaire,
  submitPublicQuestionnaire
} from './publicOnboardingApi.js';

const STEPS = ['Personal Details', 'Contact & Address', 'Documents', 'Review & Confirm'];

const EMPTY_FORM = {
  firstName: '',
  middleName: '',
  lastName: '',
  dateOfBirth: '',
  email: '',
  mobilePhone: '',
  addressLine1: '',
  addressLine2: '',
  suburb: '',
  state: '',
  postcode: '',
  country: 'Australia',
  clientConfirmed: false
};

export default function PublicOnboardingPage({ token }) {
  const [onboarding, setOnboarding] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [stepIndex, setStepIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [validationErrors, setValidationErrors] = useState({});
  const fieldRefs = {
    firstName: useRef(null),
    lastName: useRef(null),
    dateOfBirth: useRef(null),
    email: useRef(null),
    mobilePhone: useRef(null),
    addressLine1: useRef(null),
    suburb: useRef(null),
    state: useRef(null),
    postcode: useRef(null),
    country: useRef(null),
    clientConfirmed: useRef(null)
  };

  const currentStep = STEPS[stepIndex];
  const isFinalStep = stepIndex === STEPS.length - 1;
  const canSubmit = isFinalStep && form.clientConfirmed && !saving;

  useEffect(() => {
    async function loadOnboarding() {
      setLoading(true);
      setErrorMessage(null);
      try {
        const response = await getPublicOnboarding(token);
        setOnboarding(response);
        setForm(formFromQuestionnaire(response.questionnaire, response.email));
      } catch (error) {
        setOnboarding(null);
        setErrorMessage(error.message || 'This onboarding invitation is not available.');
      } finally {
        setLoading(false);
      }
    }

    loadOnboarding();
  }, [token]);

  const reviewGroups = useMemo(() => ([
    {
      title: 'Personal Details',
      rows: [
        ['First Name', form.firstName],
        ['Middle Name', form.middleName],
        ['Last Name', form.lastName],
        ['Date of Birth', form.dateOfBirth]
      ]
    },
    {
      title: 'Contact',
      rows: [
        ['Email', form.email],
        ['Mobile Number', form.mobilePhone]
      ]
    },
    {
      title: 'Residential Address',
      rows: [
        ['Address Line 1', form.addressLine1],
        ['Address Line 2', form.addressLine2],
        ['Postcode', form.postcode],
        ['State', form.state],
        ['Suburb', form.suburb],
        ['Country', form.country]
      ]
    },
    {
      title: 'Documents',
      rows: [
        ['Documents', 'Document upload will be added in a later step.']
      ]
    }
  ]), [form]);

  function updateField(event) {
    const { name, type, checked, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
      ...(name === 'postcode' ? inferStateUpdate(value) : {})
    }));
    setValidationErrors((current) => ({
      ...current,
      [name]: undefined,
      ...(name === 'postcode' && inferStateUpdate(value).state ? { state: undefined } : {})
    }));
  }

  async function nextStep() {
    if (!validateStep(stepIndex)) {
      return;
    }

    if (stepIndex >= 1 && stepIndex !== 2) {
      const saved = await saveQuestionnaire(false);
      if (!saved) {
        return;
      }
    }

    setStepIndex((current) => Math.min(current + 1, STEPS.length - 1));
  }

  function previousStep() {
    setValidationErrors({});
    setStepIndex((current) => Math.max(current - 1, 0));
  }

  async function submitQuestionnaire() {
    if (!validateStep(stepIndex)) {
      return;
    }

    const saved = await saveQuestionnaire(true);
    if (!saved) {
      return;
    }

    setSaving(true);
    setErrorMessage(null);
    try {
      await submitPublicQuestionnaire(token);
      setSubmitted(true);
    } catch (error) {
      setErrorMessage(error.message || 'Unable to submit the questionnaire.');
    } finally {
      setSaving(false);
    }
  }

  async function saveQuestionnaire(clientConfirmed) {
    setSaving(true);
    setErrorMessage(null);
    try {
      const saved = await savePublicQuestionnaire(token, buildPayload(clientConfirmed));
      setForm(formFromQuestionnaire(saved, form.email));
      return true;
    } catch (error) {
      setErrorMessage(error.message || 'Unable to save the questionnaire.');
      return false;
    } finally {
      setSaving(false);
    }
  }

  function validateStep(index) {
    const errors = {};

    if (index === 0) {
      requireField(errors, 'firstName', form.firstName, 'First Name is required.');
      requireField(errors, 'lastName', form.lastName, 'Last Name is required.');
      requireField(errors, 'dateOfBirth', form.dateOfBirth, 'Date of Birth is required.');
    }

    if (index === 1 || index === 3) {
      requireField(errors, 'email', form.email, 'Email is required.');
      if (form.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
        errors.email = 'Enter a valid email address.';
      }
      requireField(errors, 'mobilePhone', form.mobilePhone, 'Mobile Number is required.');
      requireField(errors, 'addressLine1', form.addressLine1, 'Address Line 1 is required.');
      requireField(errors, 'postcode', form.postcode, 'Postcode is required.');
      requireField(errors, 'state', form.state, 'State is required.');
      requireField(errors, 'suburb', form.suburb, 'Suburb is required.');
      requireField(errors, 'country', form.country, 'Country is required.');
    }

    if (index === 3) {
      if (!form.clientConfirmed) {
        errors.clientConfirmed = 'Please confirm the information is accurate and complete.';
      }
    }

    if (index === 3) {
      requireField(errors, 'firstName', form.firstName, 'First Name is required.');
      requireField(errors, 'lastName', form.lastName, 'Last Name is required.');
      requireField(errors, 'dateOfBirth', form.dateOfBirth, 'Date of Birth is required.');
    }

    setValidationErrors(errors);
    const firstInvalidField = Object.keys(errors)[0];
    if (firstInvalidField) {
      fieldRefs[firstInvalidField]?.current?.focus();
      return false;
    }
    return true;
  }

  function buildPayload(clientConfirmed) {
    return {
      firstName: form.firstName.trim(),
      middleName: emptyToNull(form.middleName),
      lastName: form.lastName.trim(),
      dateOfBirth: form.dateOfBirth,
      email: form.email.trim(),
      mobilePhone: form.mobilePhone.trim(),
      addressLine1: form.addressLine1.trim(),
      addressLine2: emptyToNull(form.addressLine2),
      suburb: form.suburb.trim(),
      state: form.state.trim(),
      postcode: form.postcode.trim(),
      country: form.country.trim(),
      clientConfirmed
    };
  }

  return (
    <div className="public-app">
      <AppHeader />
      <main className="public-shell">
        <section className="public-panel">

        {loading && <div className="public-loading">Loading onboarding invitation...</div>}

        {!loading && errorMessage && !onboarding && (
          <div className="public-content">
            <Message type="error">{errorMessage}</Message>
          </div>
        )}

        {!loading && onboarding && submitted && (
          <div className="public-content">
            <p className="public-greeting">Thank you, {onboarding.preferredName}.</p>
            <p className="public-copy">Your onboarding questionnaire has been submitted.</p>
          </div>
        )}

        {!loading && onboarding && !submitted && (
          <div className="public-content">
            <InvitationContext onboarding={onboarding} />

            {errorMessage && <Message type="error">{errorMessage}</Message>}

            <ol className="wizard-steps" aria-label="Questionnaire steps">
              {STEPS.map((step, index) => (
                <li key={step} className={index === stepIndex ? 'active' : index < stepIndex ? 'complete' : ''}>
                  <span>{index + 1}</span>
                  {step}
                </li>
              ))}
            </ol>

            <section className="wizard-card">
              <h2>{currentStep}</h2>

              {stepIndex === 0 && (
                <div className="wizard-form-grid">
                  <WizardField label="First Name" name="firstName" value={form.firstName} error={validationErrors.firstName} inputRef={fieldRefs.firstName} onChange={updateField} required />
                  <WizardField label="Middle Name" name="middleName" value={form.middleName} onChange={updateField} />
                  <WizardField label="Last Name" name="lastName" value={form.lastName} error={validationErrors.lastName} inputRef={fieldRefs.lastName} onChange={updateField} required />
                  <WizardField label="Date of Birth" name="dateOfBirth" type="date" value={form.dateOfBirth} error={validationErrors.dateOfBirth} inputRef={fieldRefs.dateOfBirth} onChange={updateField} required />
                </div>
              )}

              {stepIndex === 1 && (
                <div className="wizard-form-grid">
                  <WizardField label="Email" name="email" type="email" value={form.email} error={validationErrors.email} inputRef={fieldRefs.email} onChange={updateField} required />
                  <WizardField label="Mobile Number" name="mobilePhone" value={form.mobilePhone} error={validationErrors.mobilePhone} inputRef={fieldRefs.mobilePhone} onChange={updateField} required />
                  <WizardField label="Address Line 1" name="addressLine1" value={form.addressLine1} error={validationErrors.addressLine1} inputRef={fieldRefs.addressLine1} onChange={updateField} required />
                  <WizardField label="Address Line 2" name="addressLine2" value={form.addressLine2} onChange={updateField} />
                  <WizardField label="Postcode" name="postcode" value={form.postcode} error={validationErrors.postcode} inputRef={fieldRefs.postcode} onChange={updateField} required />
                  <StateField value={form.state} error={validationErrors.state} inputRef={fieldRefs.state} onChange={updateField} />
                  <WizardField label="Suburb" name="suburb" value={form.suburb} error={validationErrors.suburb} inputRef={fieldRefs.suburb} onChange={updateField} required />
                  <WizardField label="Country" name="country" value={form.country} error={validationErrors.country} inputRef={fieldRefs.country} onChange={updateField} required />
                </div>
              )}

              {stepIndex === 2 && (
                <div className="documents-placeholder">
                  <h3>Documents</h3>
                  <p>Document upload will be added in a later step.</p>
                </div>
              )}

              {stepIndex === 3 && (
                <div className="review-layout">
                  {reviewGroups.map((group) => (
                    <ReviewGroup key={group.title} title={group.title} rows={group.rows} />
                  ))}
                  <label className="confirm-field">
                    <input
                      ref={fieldRefs.clientConfirmed}
                      name="clientConfirmed"
                      type="checkbox"
                      checked={form.clientConfirmed}
                      onChange={updateField}
                      aria-invalid={Boolean(validationErrors.clientConfirmed)}
                    />
                    <span>I confirm that the information provided is accurate and complete.</span>
                  </label>
                  {validationErrors.clientConfirmed && <span className="field-error">{validationErrors.clientConfirmed}</span>}
                </div>
              )}

              <div className="wizard-actions">
                <Button variant="secondary" onClick={previousStep} disabled={stepIndex === 0 || saving}>
                  Back
                </Button>
                {!isFinalStep && (
                  <Button variant="primary" onClick={nextStep} disabled={saving}>
                    {saving ? 'Saving...' : 'Next'}
                  </Button>
                )}
                {isFinalStep && (
                  <Button variant="primary" onClick={submitQuestionnaire} disabled={!canSubmit}>
                    {saving ? 'Submitting...' : 'Submit Questionnaire'}
                  </Button>
                )}
              </div>
            </section>
          </div>
        )}
        </section>
      </main>
    </div>
  );
}

function InvitationContext({ onboarding }) {
  return (
    <div className="public-context">
      <div>
        <p className="public-greeting">Hello {onboarding.preferredName},</p>
        <p className="public-copy">Please complete your onboarding questionnaire.</p>
      </div>
      <div className="public-details compact">
        <Detail label="Invitation Email" value={onboarding.email} />
        <Detail label="Expires" value={formatDate(onboarding.expiresAt)} />
      </div>
    </div>
  );
}

function WizardField({ label, name, type = 'text', value, error, inputRef, onChange, required = false }) {
  return (
    <label className="field wizard-field">
      <span>{label}</span>
      <input
        ref={inputRef}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        required={required}
        aria-invalid={Boolean(error)}
      />
      {error && <span className="field-error">{error}</span>}
    </label>
  );
}

function StateField({ value, error, inputRef, onChange }) {
  return (
    <label className="field wizard-field">
      <span>State</span>
      <select
        ref={inputRef}
        name="state"
        value={value}
        onChange={onChange}
        required
        aria-invalid={Boolean(error)}
      >
        <option value="">Select state</option>
        {AUSTRALIAN_STATES.map((state) => (
          <option key={state} value={state}>{state}</option>
        ))}
      </select>
      {error && <span className="field-error">{error}</span>}
    </label>
  );
}

function ReviewGroup({ title, rows }) {
  return (
    <section className="review-group">
      <h3>{title}</h3>
      <dl>
        {rows.map(([label, value]) => (
          <div key={label}>
            <dt>{label}</dt>
            <dd>{value || '-'}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}

function Detail({ label, value }) {
  return (
    <div className="public-detail">
      <span>{label}</span>
      <strong>{value || '-'}</strong>
    </div>
  );
}

function formFromQuestionnaire(questionnaire, invitationEmail) {
  if (!questionnaire) {
    return {
      ...EMPTY_FORM,
      email: invitationEmail || ''
    };
  }

  return {
    firstName: questionnaire.firstName || '',
    middleName: questionnaire.middleName || '',
    lastName: questionnaire.lastName || '',
    dateOfBirth: questionnaire.dateOfBirth || '',
    email: questionnaire.email || invitationEmail || '',
    mobilePhone: questionnaire.mobilePhone || '',
    addressLine1: questionnaire.addressLine1 || '',
    addressLine2: questionnaire.addressLine2 || '',
    suburb: questionnaire.suburb || '',
    state: questionnaire.state || '',
    postcode: questionnaire.postcode || '',
    country: questionnaire.country || 'Australia',
    clientConfirmed: Boolean(questionnaire.clientConfirmed)
  };
}

function requireField(errors, fieldName, value, message) {
  if (!String(value || '').trim()) {
    errors[fieldName] = message;
  }
}

function emptyToNull(value) {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}

function inferStateUpdate(postcode) {
  const result = lookupAustralianPostcode(postcode);
  return result.state ? { state: result.state } : {};
}
