import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputSearch = ({ id, label, disabled = false }) => {
  return (
    <div className="flex items-center gap-2 w-fit">
      <label htmlFor={id}>
        <Label.Title>{label}</Label.Title>
      </label>
      <input
        type="search"
        id={id}
        disabled={disabled}
        className="border px-2 py-1 rounded w-fit"
      />
    </div>
  );
};

InputSearch.propTypes = {
  id: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
};

export default InputSearch;
