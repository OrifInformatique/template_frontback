import React, { useState } from "react";
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

  const handleCheckboxChange = (id, isChecked) => {
    const updated = isChecked
      ? [...selectedIds, id]
      : selectedIds.filter((item) => item !== id);

    setSelectedIds(updated);
    onChange(updated);
  };

  return (
    <Label required>
      <Label.Title>{label}</Label.Title>

      <div className="flex flex-col gap-2">
        {options.map(
          ({
            id,
            name,
            label,
            disabled = false,
            required = false,
            defaultChecked = false,
            labelPosition = "right",
          }) => {
            const isLeft = labelPosition === "left";
            const isDisabled = allDisabled || disabled;

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
