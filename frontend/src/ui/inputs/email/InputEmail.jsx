import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputEmail = ({
    id, name, label, disabled = false, placeholder = "", required = false
}) => {
    const [email, setEmail] = useState("");

    if (disabled) required = false;

    const handleEmailChange = (event) => {
        setEmail(event.target.value);
    }

    return (
        <Label htmlFor={id} className="flex flex-col gap-2">
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
        </Label>
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