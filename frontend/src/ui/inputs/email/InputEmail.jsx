import React, { useState } from "react";
import PropTypes from "prop-types";

const InputEmail = ({
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
    // Internal state of the input
    const [email, setEmail] = useState(value ?? defaultValue ?? "");

    if (disabled) required = false;

    const handleEmailChange = (event) => {
        if(onChangeFunction)
            onChangeFunction(event.target.value)

        setEmail(event.target.value);
    }

    return (
        <label htmlFor={id} className="flex flex-col gap-2">
            <div>
                {required && <span className="text-red-700">* </span>}
                <span className="text-primary font-medium">{label}</span>
            </div>
            <input
                className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary"
                type="email"
                id={id}
                name={name}
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
    value: PropTypes.string,
    defaultValue: PropTypes.string,
    onChangeFunction: PropTypes.func,
    disabled: PropTypes.bool,
    placeholder: PropTypes.string,
    required: PropTypes.bool
}

export default InputEmail;