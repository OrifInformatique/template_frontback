import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputCheckbox = ({
  options = [],
  onChange = () => {},
  allDisabled = false,
  label = "Sélectionner une option",
}) => {
  const [selectedIds, setSelectedIds] = useState(
    options.filter((o) => o.defaultChecked).map((o) => o.id)
  );

  const [longestLabelWidth, setLongestLabelWidth] = useState(0);
  const labelRefs = [];

  const handleCheckboxChange = (id, isChecked) => {
    const updated = isChecked
      ? [...selectedIds, id]
      : selectedIds.filter((item) => item !== id);

    setSelectedIds(updated);
    onChange(updated);
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
              name,
              label,
              disabled = false,
              required = false,
              defaultChecked = false,
              labelPosition = "right",
            },
            index
          ) => {
            const isLeft = labelPosition === "left";
            const isDisabled = allDisabled || disabled;

            return (
              <div
                key={id}
                className="grid grid-cols-[auto_1fr] items-center gap-2"
              >
                {isLeft && (
                  <span
                    ref={(el) => (labelRefs[index] = el)}
                    className="text-sm"
                    style={{ minWidth: `${longestLabelWidth}px`, textAlign: "left" }}
                  >
                    <Label.Title unstyled>{label}</Label.Title>
                  </span>
                )}

                <label
                  htmlFor={id}
                  className="flex items-center gap-2 w-fit cursor-pointer"
                >
                  <input
                    className="disabled:bg-disabled focus:border-primary"
                    type="checkbox"
                    id={id}
                    name={name}
                    defaultChecked={defaultChecked}
                    disabled={isDisabled}
                    required={required}
                    onChange={(e) =>
                      handleCheckboxChange(id, e.target.checked)
                    }
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

InputCheckbox.propTypes = {
  options: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      disabled: PropTypes.bool,
      required: PropTypes.bool,
      defaultChecked: PropTypes.bool,
      labelPosition: PropTypes.oneOf(["left", "right"]),
    })
  ).isRequired,
  onChange: PropTypes.func,
  allDisabled: PropTypes.bool,
  label: PropTypes.string,
};

export default InputCheckbox;
