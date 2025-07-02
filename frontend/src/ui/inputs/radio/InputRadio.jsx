import React, { useState, useEffect } from "react";
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

  const [longestLabelWidth, setLongestLabelWidth] = useState(0);
  const labelRefs = [];

  const handleRadioChange = (id) => {
    setSelectedId(id);
    onChange(id);
  };

  useEffect(() => {
    const widths = labelRefs.map(ref => ref?.offsetWidth || 0);
    const maxWidth = Math.max(...widths);
    if (maxWidth > 0) {
      setLongestLabelWidth(maxWidth);
    }
  }, [options]);

  return (
    <Label required>
      <Label.Title>{label}</Label.Title>

      <div className="flex flex-col gap-2">
        {options.map(
          (
            {
              id,
              label,
              disabled = false,
              required = false,
              defaultChecked = false,
              labelPosition = "right",
            },
            index
          ) => {
            const isLeft = labelPosition === "left";
            const isDisabled = disabledAll || disabled;

            return (
              <div
                key={id}
                className="grid grid-cols-[auto_1fr] items-center gap-2"
              >
                {isLeft && (
                  <span
                    ref={(el) => (labelRefs[index] = el)}
                    className="text-sm"
                    style={{
                      minWidth: `${longestLabelWidth}px`,
                      textAlign: "left",
                    }}
                  >
                    <Label.Title unstyled>{label}</Label.Title>
                  </span>
                )}

                <label
                  htmlFor={id}
                  className="flex items-center gap-2 w-fit cursor-pointer"
                >
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
                    <Label.Title unstyled className="w-fit">
                      {label}
                    </Label.Title>
                  )}
                </label>
              </div>
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
