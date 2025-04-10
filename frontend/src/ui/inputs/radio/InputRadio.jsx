import React from "react";
import PropTypes from "prop-types";

const InputRadio = ({ id, label, disabled = false }) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <input
                id={id}
                type="radio"
                disabled={disabled}
            />
            <span>{label}</span>
        </label>
    );
}

InputRadio.propTypes = {
    
}

export default InputRadio;