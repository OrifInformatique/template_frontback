import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

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
        <Label htmlFor={id} required={required}>
            <Label.Title>{label}</Label.Title>
            <input
                className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary w-fit"
                type="number"
                id={id}
                name={name}
                min={min}
                max={max}
                value={internalValue}
                onChange={handleNumberChange}
                disabled={disabled}
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
    value: PropTypes.number,
    defaultValue: PropTypes.number,
    onChangeFunction: PropTypes.func,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputNumber;