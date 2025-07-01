import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputRadio = ({ id, label, disabled = false, labelPosition = "right" }) => {
  const isLeft = labelPosition === "left";

  return (
    <label htmlFor={id} className="flex items-center gap-2 cursor-pointer w-fit">
      {isLeft && <Label.Title unstyled>{label}</Label.Title>}

      <input
        type="radio"
        id={id}
        disabled={disabled}
        className="accent-primary"
      />

      {!isLeft && <Label.Title unstyled>{label}</Label.Title>}
    </label>
  );
};

InputRadio.propTypes = {
  id: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  labelPosition: PropTypes.oneOf(["left", "right"]),
};

export default InputRadio;
