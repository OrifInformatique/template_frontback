import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";
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
    required = false
}) => {
    const [showPassword, setShowPassword] = useState(false);

    if (disabled) required = false;

    const togglePasswordVisibility = (e) => {
        e.preventDefault();
        setShowPassword(prev => !prev);
    }

    return (
        <Label htmlFor={id} required={required}>
            <Label.Title>{label}</Label.Title>
            <div className="flex items-center w-fit">
                <input
                    className="rounded-md pr-10 w-full disabled:bg-disabled focus:ring-primary focus:border-primary w-fit"
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
        </Label>
    );
};

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
};

export default InputPassword;
