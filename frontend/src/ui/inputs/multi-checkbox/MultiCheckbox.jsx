import React, { useState } from "react";
import PropTypes from "prop-types";
import InputCheckbox from "../checkbox/InputCheckbox";

const MultiCheckbox = ({ options, onChange }) => {
  const [selectedIds, setSelectedIds] = useState(
    options.filter(opt => opt.defaultChecked).map(opt => opt.id)
  );

  const handleCheckboxChange = (id, isChecked) => {
    let updatedSelection;
    if (isChecked) {
      updatedSelection = [...selectedIds, id];
    } else {
      updatedSelection = selectedIds.filter(selectedId => selectedId !== id);
    }

    setSelectedIds(updatedSelection);
    onChange && onChange(updatedSelection);
  };

  return (
    <div className="flex flex-col gap-2">
      {options.map(option => (
        <InputCheckbox
          key={option.id}
          id={option.id}
          name={option.name}
          label={option.label}
          defaultChecked={option.defaultChecked}
          disabled={option.disabled}
          required={option.required}
          onChange={(e) => handleCheckboxChange(option.id, e.target.checked)}
        />
      ))}
    </div>
  );
};

MultiCheckbox.propTypes = {
  options: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      defaultChecked: PropTypes.bool,
      disabled: PropTypes.bool,
      required: PropTypes.bool
    })
  ).isRequired,
  onChange: PropTypes.func
};

export default MultiCheckbox;
