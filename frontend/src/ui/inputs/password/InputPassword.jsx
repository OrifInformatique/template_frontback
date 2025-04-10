import React from "react";
import PropTypes from "prop-types";

const InputPassword = ({ id, label, disabled = false }) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <span>{label}</span>
            <input
                id={id}
                type="password"
                disabled={disabled}
            />
        </label>
    );
}

InputPassword.propTypes = {
    
}

export default InputPassword;