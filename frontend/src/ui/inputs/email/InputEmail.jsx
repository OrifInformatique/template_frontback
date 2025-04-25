import React, { useState } from "react";
import PropTypes from "prop-types";

const InputEmail = ({
    id, name, label, disabled = false, placeholder = "", required = false
}) => {
    const [email, setEmail] = useState("");

    const handleEmailChange = (event) => {
        setEmail(event.target.value);
    }

    return (
        <label htmlFor={id} className="flex flex-col gap-2">
            <span>{label}</span>
            <input
                id={id}
                name={name}
                type="email"
                value={email}
                onChange={handleEmailChange}
                disabled={disabled}
                placeholder={placeholder}
                required={required}
            />
        </label>
    );
}

InputEmail.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
    placeholder: PropTypes.string,
    requried: PropTypes.bool
}

export default InputEmail;