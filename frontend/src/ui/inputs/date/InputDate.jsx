import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputDate = ({ id, name, label, disabled = false, required = false }) => {
  const [selectedDate, setSelectedDate] = useState("");

  if (disabled) required = false;

  const handleDateChange = (event) => {
    setSelectedDate(event.target.value);
  };

  return (
    <Label htmlFor={id} required={required}>
      {label}
      <input
        className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary"
        type="date"
        id={id}
        name={name}
        value={selectedDate}
        onChange={handleDateChange}
        disabled={disabled}
        required={required}
      />
    </Label>
  );
};

InputDate.propTypes = {
  id: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  required: PropTypes.bool
};

export default InputDate;
