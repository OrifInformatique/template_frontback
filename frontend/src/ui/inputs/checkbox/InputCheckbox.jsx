import React, { useState } from "react";
import PropTypes from "prop-types";

const InputCheckbox = ({
    id, name, label, defaultChecked = false, disabled = false, required = false
}) => {
    const [isChecked, setIsChecked] = useState(defaultChecked);

    const handleCheckboxChange = (event) => {
        setIsChecked(event.target.checked);
    }

    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <input
                id={id}
                name={name}
                type="checkbox"
                checked={isChecked}
                onChange={handleCheckboxChange}
                disabled={disabled}
                required={required}
            />
            <span>{label}</span>
        </label>
    );
}

InputCheckbox.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    defaultChecked: PropTypes.bool,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputCheckbox;