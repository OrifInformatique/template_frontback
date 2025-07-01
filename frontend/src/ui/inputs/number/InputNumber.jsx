import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputNumber = ({
    id,
    name,
    label = null,
    min = null,
    max = null,
    disabled = false,
    required = false
}) => {
    const [value, setValue] = useState(0);

    if (disabled) required = false;

    const handleNumberChange = (event) => {
        let inputValue = event.target.value;

        if (inputValue === "") {
            setValue(min);
            return;
        }

        const normalizedValue = inputValue.replace(/^0+(?!$)/, "");
        const numericValue = Number(normalizedValue);

        if (numericValue < min) {
            setValue(min);
        } else if (numericValue > max) {
            setValue(max);
        } else {
            setValue(normalizedValue);
        }
    }

    return (
        <Label htmlFor={id} required>
            <Label.Title>{label}</Label.Title>
            <input
                className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary w-fit"
                type="number"
                id={id}
                name={name}
                min={min}
                max={max}
                value={value}
                onChange={handleNumberChange}
                disabled={disabled}
                required={required}
            />
        </Label>
    );
}

InputNumber.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    min: PropTypes.number,
    max: PropTypes.number,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputNumber;