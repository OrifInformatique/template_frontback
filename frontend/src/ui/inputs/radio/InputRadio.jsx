import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label"; 

const InputRadio = ({ id, label, disabled = false }) => {
  return (
    <Label htmlFor={id} inlineLeft>
      <>
        <input
          type="radio"
          id={id}
          disabled={disabled}
        />
        <span>{label}</span>
      </>
    </Label>
  );
};

InputRadio.propTypes = {
  id: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
};

export default InputRadio;
