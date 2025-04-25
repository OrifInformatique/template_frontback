import React from "react";
import PropTypes from "prop-types";

const InputNumber = ({
    id, name, label = null, min = null, max = null, disabled = false, required = false
}) => {
    return (
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
            {label && <span>{label}</span>}
            <input
                id={id}
                name={name}
                type="number"
                min={min}
                max={max}
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
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputNumber;