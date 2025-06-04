import React, { useState } from "react";
import PropTypes from "prop-types";

// UI elements
import Icon from "../../icon/Icon";

const InputPassword = ({
    id,
    name,
    label,
    value = null,
    defaultValue = null,
    onChangeFunction = null,
    disabled = false,
    placeholder = "",
    required = true
}) => {
    const [showPassword, setShowPassword] = useState(false);

    if (disabled) required = false;

    const togglePasswordVisibility = (e) => {
        e.preventDefault();
        setShowPassword(prev => !prev);
    }

    return (
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
            <div>
                {required && <span className="text-red-700">* </span>}
                <span className="text-primary font-medium">{label}</span>
            </div>
            <div className="flex items-center">
                <input
                    className="rounded-md pr-10 disabled:bg-disabled focus:ring-primary focus:border-primary"
                    type={showPassword ? "text" : "password"}
                    id={id}
                    name={name}
                    {...value !== null
                        ? { value: value }
                        : { defaultValue: defaultValue }
                    }
                    onChange={onChangeFunction}
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
        </label>
    );
}

InputPassword.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    value: PropTypes.string,
    defaultValue: PropTypes.string,
    onChangeFunction: PropTypes.func,
    disabled: PropTypes.bool,
    placeholder: PropTypes.string,
    required: PropTypes.bool
}

export default InputPassword;