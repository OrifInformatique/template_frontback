import React, { useState } from "react";
import PropTypes from "prop-types";

const InputNumber = ({
    id, name, label = null, min = null, max = null, disabled = false, required = false
}) => {
    const [value, setValue] = useState(0);

    const handleNumberChange = (event) => {
        let inputValue = event.target.value;
        console.log(inputValue);

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
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
            {label && <span>{label}</span>}
            <input
                id={id}
                name={name}
                type="number"
                min={min}
                max={max}
                value={value}
                onChange={handleNumberChange}
                disabled={disabled}
                required={required}
            />
            <p>{value}</p>
        </label>
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