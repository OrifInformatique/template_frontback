import React from "react";
import PropTypes from "prop-types";

const InputEmail = ({ id, label, disabled = false }) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <span>{label}</span>
            <input
                id={id}
                type="email"
                disabled={disabled}
            />
        </label>
    );
}

InputEmail.propTypes = {
    
}

export default InputEmail;