import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputRadio = ({
  options = [],
  onChange = () => {},
  name,
  disabledAll = false,
  label = "Sélectionner une option",
}) => {
  const [selectedId, setSelectedId] = useState(
    options.find((o) => o.defaultChecked)?.id || ""
  );

  const handleRadioChange = (id) => {
    setSelectedId(id);
    onChange(id);
  };

  return (
    <Label required>
      <Label.Title>{label}</Label.Title>

      <div className="flex flex-col gap-2">
        {options.map(
          ({
            id,
            label,
            disabled = false,
            required = false,
            defaultChecked = false,
            labelPosition = "right",
          }) => {
            const isLeft = labelPosition === "left";
            const isDisabled = disabledAll || disabled;

            return (
              <label
                key={id}
                htmlFor={id}
                className="flex items-center gap-2 cursor-pointer w-fit"
              >
                {isLeft && (
                  <Label.Title unstyled className="w-40 text-right">
                    {label}
                  </Label.Title>
                )}

                <input
                  type="radio"
                  id={id}
                  name={name}
                  checked={selectedId === id}
                  disabled={isDisabled}
                  required={required}
                  onChange={() => handleRadioChange(id)}
                  className="accent-primary disabled:bg-disabled"
                />

                {!isLeft && (
                  <Label.Title unstyled className="w-40">
                    {label}
                  </Label.Title>
                )}
              </label>
            );
          }
        )}
      </div>
    </Label>
  );
};

InputRadio.propTypes = {
  options: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      disabled: PropTypes.bool,
      required: PropTypes.bool,
      defaultChecked: PropTypes.bool,
      labelPosition: PropTypes.oneOf(["left", "right"]),
    })
  ).isRequired,
  onChange: PropTypes.func,
  name: PropTypes.string.isRequired,
  label: PropTypes.string,
  disabledAll: PropTypes.bool,
};

export default InputRadio;
