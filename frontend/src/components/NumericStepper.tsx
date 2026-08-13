import type React from "react";

interface NumericStepperProps {
  value: number;
  onChange: (value: number) => void;
  min?: number;
  max?: number;
  step?: number;
  disabled?: boolean;
}

export function NumericStepper({
  value,
  onChange,
  min = 1,
  max = 999,
  step = 1,
  disabled = false,
}: NumericStepperProps) {
  const decrement = () => {
    if (!disabled && (min === undefined || value > min)) {
      onChange(Math.max(min, value - step));
    }
  };

  const increment = () => {
    if (!disabled && (max === undefined || value < max)) {
      onChange(Math.min(max, value + step));
    }
  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const parsed = Number.parseInt(event.target.value, 10);
    if (!Number.isNaN(parsed)) {
      let clamped = parsed;
      if (min !== undefined) clamped = Math.max(min, clamped);
      if (max !== undefined) clamped = Math.min(max, clamped);
      onChange(clamped);
    }
  };

  return (
    <div className={`stepper ${disabled ? "desactivado" : ""}`}>
      <button
        type="button"
        className="stepper-btn"
        onClick={decrement}
        disabled={disabled || (min !== undefined && value <= min)}
        aria-label="Disminuir"
      >
        −
      </button>
      <input
        type="number"
        className="stepper-input"
        value={value}
        onChange={handleChange}
        min={min}
        max={max}
        step={step}
        disabled={disabled}
      />
      <button
        type="button"
        className="stepper-btn"
        onClick={increment}
        disabled={disabled || (max !== undefined && value >= max)}
        aria-label="Aumentar"
      >
        +
      </button>
    </div>
  );
}
