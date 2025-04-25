import React, { useState } from "react";
import PropTypes from "prop-types";

// UI elements
import Icon from "../../icon/Icon";

const InputPassword = ({
    id, name, label, disabled = false, placeholder = "", required = true
}) => {
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);

    const handlePasswordChange = (event) => {
        setPassword(event.target.value);
    }

    const togglePasswordVisibility = (e) => {
        e.preventDefault();
        setShowPassword(!showPassword);
    }

    return (
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
            <span>{label}</span>
            <div className="flex items-center">
                <input
                    id={id}
                    name={name}
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={handlePasswordChange}
                    disabled={disabled}
                    placeholder={placeholder}
                    required={required}
                    className="pr-10"
                />
                <button className="-ml-8" disabled={disabled} onClick={togglePasswordVisibility}>
                    {showPassword
                        ? <Icon name="eye-slash" size={6} />
                        : <Icon name="eye" size={6} />}
                </button>
            </div>
        </label>
    );
}

InputPassword.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
    placeholder: PropTypes.string,
    required: PropTypes.bool
}

export default InputPassword;