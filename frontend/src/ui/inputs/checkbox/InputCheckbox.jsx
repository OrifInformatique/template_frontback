import React from "react";
import PropTypes from "prop-types";

const InputCheckbox = ({
    id, label, onChange, disabled = false
}) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <input
                id={id}
                type="checkbox"
                onChange={onChange}
                disabled={disabled}
            />
            <span>{label}</span>
        </label>
    );
}

InputCheckbox.propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    disabled: PropTypes.bool
}

export default InputCheckbox;