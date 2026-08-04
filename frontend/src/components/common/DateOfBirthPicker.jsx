import { useEffect, useMemo, useRef, useState } from 'react';
import { createPortal } from 'react-dom';

const DATE_PICKER_LOCALE = 'en-AU';
const DATE_PICKER_WEEKDAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const DATE_PICKER_MONTHS = Array.from({ length: 12 }, (_, monthIndex) =>
  new Intl.DateTimeFormat(DATE_PICKER_LOCALE, { month: 'long' }).format(new Date(2000, monthIndex, 1))
);

export default function DateOfBirthPicker({
  value,
  onChange,
  inputRef,
  error,
  label = 'Date of Birth',
  required = true,
  showLabel = true,
  className = 'field wizard-field date-picker-field',
  inputClassName = '',
  usePortal = false
}) {
  const pickerRef = useRef(null);
  const popoverRef = useRef(null);
  const fallbackInputRef = useRef(null);
  const buttonRef = inputRef || fallbackInputRef;
  const today = useMemo(() => startOfDay(new Date()), []);
  const selectedDate = parseIsoDate(value);
  const [open, setOpen] = useState(false);
  const [popoverStyle, setPopoverStyle] = useState({});
  const [visibleYear, setVisibleYear] = useState(() => (selectedDate || today).getFullYear());
  const [visibleMonth, setVisibleMonth] = useState(() => (selectedDate || today).getMonth());
  const years = useMemo(() => {
    const currentYear = today.getFullYear();
    return Array.from({ length: 121 }, (_, index) => currentYear - index);
  }, [today]);

  useEffect(() => {
    if (selectedDate) {
      setVisibleYear(selectedDate.getFullYear());
      setVisibleMonth(selectedDate.getMonth());
    }
  }, [value]);

  useEffect(() => {
    if (!open) {
      return undefined;
    }

    function updatePopoverPosition() {
      if (!usePortal || !buttonRef.current) {
        return;
      }

      const rect = buttonRef.current.getBoundingClientRect();
      setPopoverStyle({
        position: 'fixed',
        top: `${rect.bottom + 4}px`,
        left: `${rect.left}px`,
        width: '292px'
      });
    }

    function handleMouseDown(event) {
      if (
        pickerRef.current &&
        !pickerRef.current.contains(event.target) &&
        popoverRef.current &&
        !popoverRef.current.contains(event.target)
      ) {
        setOpen(false);
      }
    }

    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        setOpen(false);
        buttonRef.current?.focus();
      }
    }

    updatePopoverPosition();
    document.addEventListener('mousedown', handleMouseDown);
    document.addEventListener('keydown', handleKeyDown);
    window.addEventListener('resize', updatePopoverPosition);
    window.addEventListener('scroll', updatePopoverPosition, true);

    return () => {
      document.removeEventListener('mousedown', handleMouseDown);
      document.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('resize', updatePopoverPosition);
      window.removeEventListener('scroll', updatePopoverPosition, true);
    };
  }, [open, buttonRef, usePortal]);

  const days = calendarDays(visibleYear, visibleMonth);

  function selectDate(date) {
    if (date > today) {
      return;
    }

    onChange(toIsoDate(date));
    setOpen(false);
    buttonRef.current?.focus();
  }

  function changeMonth(event) {
    const nextMonth = Number(event.target.value);
    setVisibleMonth(nextMonth);
    if (new Date(visibleYear, nextMonth, 1) > new Date(today.getFullYear(), today.getMonth(), 1)) {
      setVisibleYear(today.getFullYear());
      setVisibleMonth(today.getMonth());
    }
  }

  function changeYear(event) {
    const nextYear = Number(event.target.value);
    setVisibleYear(nextYear);
    if (new Date(nextYear, visibleMonth, 1) > new Date(today.getFullYear(), today.getMonth(), 1)) {
      setVisibleMonth(today.getMonth());
    }
  }

  const popover = (
    <div
      ref={popoverRef}
      className="date-picker-popover"
      role="dialog"
      aria-label="Choose date of birth"
      style={popoverStyle}
    >
      <div className="date-picker-header">
        <div className="date-picker-selects">
          <select value={visibleMonth} onChange={changeMonth} aria-label="Month">
            {DATE_PICKER_MONTHS.map((monthName, monthIndex) => (
              <option
                key={monthName}
                value={monthIndex}
                disabled={visibleYear === today.getFullYear() && monthIndex > today.getMonth()}
              >
                {monthName}
              </option>
            ))}
          </select>
          <select value={visibleYear} onChange={changeYear} aria-label="Year">
            {years.map((year) => (
              <option key={year} value={year}>{year}</option>
            ))}
          </select>
        </div>
      </div>
      <div className="date-picker-weekdays">
        {DATE_PICKER_WEEKDAYS.map((weekday) => (
          <span key={weekday}>{weekday}</span>
        ))}
      </div>
      <div className="date-picker-grid">
        {days.map((date, index) => {
          if (!date) {
            return <span key={`empty-${index}`} className="date-picker-empty" />;
          }

          const disabled = date > today;
          const selected = selectedDate && sameDay(date, selectedDate);

          return (
            <button
              key={toIsoDate(date)}
              type="button"
              className={selected ? 'date-picker-day selected' : 'date-picker-day'}
              onClick={() => selectDate(date)}
              disabled={disabled}
              aria-pressed={selected}
            >
              {date.getDate()}
            </button>
          );
        })}
      </div>
    </div>
  );

  return (
    <label className={className} ref={pickerRef}>
      {showLabel && <DatePickerLabel label={label} required={required} />}
      <button
        ref={buttonRef}
        type="button"
        className={`date-picker-input ${inputClassName}`}
        onClick={() => setOpen((current) => !current)}
        aria-invalid={Boolean(error)}
        aria-haspopup="dialog"
        aria-expanded={open}
      >
        <span>{formatDisplayDate(value) || 'DD/MM/YYYY'}</span>
      </button>
      {open && (usePortal ? createPortal(popover, document.body) : popover)}
    </label>
  );
}

function DatePickerLabel({ label, required }) {
  return (
    <span>
      {label}
      {required && <span className="required-indicator" aria-hidden="true"> *</span>}
    </span>
  );
}

function parseIsoDate(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value || '')) {
    return null;
  }

  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  if (
    date.getFullYear() !== year ||
    date.getMonth() !== month - 1 ||
    date.getDate() !== day
  ) {
    return null;
  }

  return date;
}

function formatDisplayDate(value) {
  const date = parseIsoDate(value);
  if (!date) {
    return '';
  }

  return [
    String(date.getDate()).padStart(2, '0'),
    String(date.getMonth() + 1).padStart(2, '0'),
    String(date.getFullYear())
  ].join('/');
}

function toIsoDate(date) {
  return [
    String(date.getFullYear()),
    String(date.getMonth() + 1).padStart(2, '0'),
    String(date.getDate()).padStart(2, '0')
  ].join('-');
}

function startOfDay(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function calendarDays(year, month) {
  const firstDay = new Date(year, month, 1);
  const firstWeekday = (firstDay.getDay() + 6) % 7;
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const days = Array.from({ length: firstWeekday }, () => null);

  for (let day = 1; day <= daysInMonth; day += 1) {
    days.push(new Date(year, month, day));
  }

  return days;
}

function sameDay(firstDate, secondDate) {
  return (
    firstDate.getFullYear() === secondDate.getFullYear() &&
    firstDate.getMonth() === secondDate.getMonth() &&
    firstDate.getDate() === secondDate.getDate()
  );
}
