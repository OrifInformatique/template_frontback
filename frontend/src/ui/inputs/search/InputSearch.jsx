import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputSearch = ({ id, label, disabled = false, required = false }) => {
  return (
    <Label htmlFor={id} required={required}>
      <Label.Title>{label}</Label.Title>
      <input
        type="search"
        id={id}
        disabled={disabled}
        className="border px-2 py-1 rounded w-full max-w-sm"
      />
    </Label>
  );
};

InputSearch.propTypes = {
  id: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  required: PropTypes.bool, 
};

export default InputSearch;
