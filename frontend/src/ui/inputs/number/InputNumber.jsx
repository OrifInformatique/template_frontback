import React, { useState } from "react";
import PropTypes from "prop-types";

const InputNumber = ({
    id,
    name,
    label = null,
    min = -Infinity,
    max = Infinity,
    value = null,
    defaultValue = null,
    onChangeFunction = null,
    disabled = false,
    required = false
}) => {
    // Internal state of the input
    const [internalValue, setInternalValue] = useState(value ?? defaultValue ?? "");

    if (disabled) required = false;

    const handleNumberChange = (event) => {
        let inputValue = event.target.value;

        if (inputValue === "") {
            if(onChangeFunction)
                onChangeFunction(min);

            setInternalValue(min);
            return;
        }

        const normalizedValue = inputValue.replace(/^0+(?!$)/, "");
        const numericValue = Number(normalizedValue);

        if (numericValue < min) {
            if(onChangeFunction)
                onChangeFunction(min);

            setInternalValue(min);
        } else if (numericValue > max) {
            if(onChangeFunction)
                onChangeFunction(max);

            setInternalValue(max);
        } else {
            if(onChangeFunction)
                onChangeFunction(normalizedValue);

            setInternalValue(normalizedValue);
        }
    }

    return (
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
        <div>
            {required && <span className="text-red-700">* </span>}
            <span className="text-primary font-medium">{label}</span>
        </div>
            <input
                className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary"
                type="number"
                id={id}
                name={name}
                min={min}
                max={max}
                value={internalValue}
                onChange={handleNumberChange}
                disabled={disabled}
                required={required}
            />
        </label>
    );
}

InputNumber.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    min: PropTypes.number,
    max: PropTypes.number,
    value: PropTypes.number,
    defaultValue: PropTypes.number,
    onChangeFunction: PropTypes.func,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputNumber;