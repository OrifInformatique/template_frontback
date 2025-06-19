import React, { useState } from "react";
import PropTypes from "prop-types";


const InputCheckbox = ({ options = [], onChange = () => {} }) => {
  const [selectedIds, setSelectedIds] = useState(
    options.filter(o => o.defaultChecked).map(o => o.id)
  );

  const handleCheckboxChange = (id, isChecked) => {
    const updated = isChecked
      ? [...selectedIds, id]
      : selectedIds.filter(item => item !== id);

    setSelectedIds(updated);
    onChange(updated); 
  };

  return (
    <div className="flex flex-col gap-2">
      {options.map(({ id, name, label, disabled = false, required = false, defaultChecked = false }) => (
        <label key={id} htmlFor={id} className="flex gap-2 items-center">
          <input
            type="checkbox"
            id={id}
            name={name}
            defaultChecked={defaultChecked}
            disabled={disabled}
            required={required}
            onChange={e => handleCheckboxChange(id, e.target.checked)}
          />
          <span>{label}</span>
        </label>
      ))}
    </div>
  );
};

InputCheckbox.propTypes = {
  options: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      disabled: PropTypes.bool,
      required: PropTypes.bool,
      defaultChecked: PropTypes.bool
    })
  ).isRequired,
  onChange: PropTypes.func
};

export default InputCheckbox;
