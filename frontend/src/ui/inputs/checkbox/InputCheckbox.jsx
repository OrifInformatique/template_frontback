import { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputCheckbox = ({
  id,
  name,
  label,
  onChange = () => {},
  defaultChecked = false,
  disabled = false,
  required = false,
  labelPosition = "right",
}) => {
  const [isChecked, setIsChecked] = useState(defaultChecked);

  if (disabled && !defaultChecked) required = false;

  const handleCheckboxChange = (event) => {
    setIsChecked(event.target.checked);
    onChange(event);
  };

  const isLeft = labelPosition === "left";

  return (
    <label
      htmlFor={id}
      className={`flex items-center gap-2 w-fit cursor-pointer ${
        isLeft ? "flex-row-reverse" : ""
      }`}
    >
      <input
        type="checkbox"
        id={id}
        name={name}
        checked={isChecked}
        onChange={handleCheckboxChange}
        disabled={disabled}
        required={required}
      />
      <Label.Title unstyled>{label}</Label.Title>
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
  required: PropTypes.bool,
  labelPosition: PropTypes.oneOf(["left", "right"]),
};

export default InputCheckbox;
