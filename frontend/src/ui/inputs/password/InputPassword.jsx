import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";
import Icon from "../../icon/Icon";

const InputPassword = ({
    id, name, label, disabled = false, placeholder = "", required = true
}) => {
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);

    if (disabled) required = false;

    const handlePasswordChange = (event) => {
        setPassword(event.target.value);
    };

    const togglePasswordVisibility = (e) => {
        e.preventDefault();
        setShowPassword(!showPassword);
    };

    return (
        <Label htmlFor={id} required={required}>
            {label}
            <div className="flex items-center w-full">
                <input
                    className="rounded-md pr-10 w-full disabled:bg-disabled focus:ring-primary focus:border-primary"
                    type={showPassword ? "text" : "password"}
                    id={id}
                    name={name}
                    value={password}
                    onChange={handlePasswordChange}
                    disabled={disabled}
                    placeholder={placeholder}
                    required={required}
                />
                <button
                    className="-ml-8"
                    disabled={disabled}
                    onClick={togglePasswordVisibility}
                >
                    {showPassword
                        ? <Icon name="eye-slash" size="6" />
                        : <Icon name="eye" size="6" />}
                </button>
            </div>
        </Label>
    );
};

InputPassword.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
    placeholder: PropTypes.string,
    required: PropTypes.bool
};

export default InputPassword;
