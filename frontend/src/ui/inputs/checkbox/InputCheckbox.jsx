import { useState } from "react";
import PropTypes from "prop-types";

const InputCheckbox = ({
  id,
  name,
  label,
  onChange = () => {},
  defaultChecked = false,
  disabled = false,
  required = false
}) => {
  const [isChecked, setIsChecked] = useState(defaultChecked);

  if (disabled && !defaultChecked) required = false;

  const handleCheckboxChange = (event) => {
    const checked = event.target.checked;
    setIsChecked(checked);      
    onChange(event);              
  };

  return (
    <label htmlFor={id} className="flex gap-2 items-center">
      <input
        type="checkbox"
        id={id}
        name={name}
        checked={isChecked}
        onChange={handleCheckboxChange}
        disabled={disabled}
        required={required}
      />
      <span>{label}</span>
    </label>
  );
};

InputCheckbox.propTypes = {
  id: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  onChange: PropTypes.func,
  defaultChecked: PropTypes.bool,
  disabled: PropTypes.bool,
  required: PropTypes.bool
};

export default InputCheckbox;
