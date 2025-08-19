import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

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
    if (disabled) required = false;

    return (
        <Label htmlFor={id} required={required}>
            <Label.Title>{label}</Label.Title>
            <input
                className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary w-fit"
                type="email"
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
        </Label>
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