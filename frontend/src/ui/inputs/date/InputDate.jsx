import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputDate = ({
  id,
  name,
  label,
  value = null,
  defaultValue = null,
  onChangeFunction = null,
  disabled = false,
  required = false
}) => {
  const isRequired = !disabled && required;

  return (
    <Label htmlFor={id} required={isRequired}>
      <Label.Title>{label}</Label.Title>
      <input
        className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary w-fit"
        type="date"
        id={id}
        name={name}
        {...(value !== null
          ? { value }
          : { defaultValue })}
        onChange={onChangeFunction}
        disabled={disabled}
        required={isRequired}
      />
    </Label>
  );
};

InputDate.propTypes = {
  id: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  value: PropTypes.string,
  defaultValue: PropTypes.string,
  onChangeFunction: PropTypes.func,
  disabled: PropTypes.bool,
  required: PropTypes.bool
};

export default InputDate;
