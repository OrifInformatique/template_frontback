import React, { useState, useEffect, useRef } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputRadio = ({
  options = [],
  onChange = () => {},
  name,
  disabledAll = false,
  label = "Sélectionner une option",
  required = false,
}) => {
  const [selectedId, setSelectedId] = useState(
    options.find((o) => o.defaultChecked)?.id || ""
  );

  const labelRefs = useRef([]);
  const [longestLabelWidth, setLongestLabelWidth] = useState(0);

  const handleRadioChange = (id) => {
    setSelectedId(id);
    onChange(id);
  };

  useEffect(() => {
    const widths = labelRefs.current.map(ref => ref?.offsetWidth || 0);
    const maxWidth = Math.max(...widths);
    if (maxWidth > 0) {
      setLongestLabelWidth(maxWidth);
    }
  }, [options]);

  return (
    <Label required={required}>
      <Label.Title>{label}</Label.Title>
      <div className="flex flex-col gap-2">
        {options.map(
          (
            {
              id,
              label,
              disabled = false,
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
                className="flex items-center"
              >
                {isLeft && (
                  <span
                    ref={el => (labelRefs.current[index] = el)}
                    className="text-sm"
                    style={{
                      minWidth: `${longestLabelWidth}px`,
                      textAlign: "left",
                      marginRight: "0.5rem",
                    }}
                  >
                    <label
                      htmlFor={id}
                      className="cursor-pointer w-full"
                      style={{ display: "block" }}
                    >
                      <Label.Title unstyled>{label}</Label.Title>
                    </label>
                  </span>
                )}
                <input
                  type="radio"
                  id={id}
                  name={name}
                  checked={selectedId === id}
                  disabled={isDisabled}
                  onChange={() => handleRadioChange(id)}
                  className="disabled:bg-disabled focus:border-primary disabled:hover:bg-disabled disabled:focus:outline-none disabled:cursor-not-allowed"
                  style={{ marginRight: isLeft ? 0 : "0.5rem" }}
                />
                {!isLeft && (
                  <span
                    ref={el => (labelRefs.current[index] = el)}
                    className="text-sm"
                  >
                    <label
                      htmlFor={id}
                      className="cursor-pointer w-full"
                      style={{ display: "block" }}
                    >
                      <Label.Title unstyled>{label}</Label.Title>
                    </label>
                  </span>
                )}
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
      defaultChecked: PropTypes.bool,
      labelPosition: PropTypes.oneOf(["left", "right"]),
    })
  ).isRequired,
  onChange: PropTypes.func,
  name: PropTypes.string.isRequired,
  label: PropTypes.string,
  disabledAll: PropTypes.bool,
  required: PropTypes.bool,
};

export default InputRadio;