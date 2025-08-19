import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputText = ({
    id,
    name,
    label,
    value = null,
    defaultValue = null,
    onChangeFunction = null,
    disabled = false,
    required = false
}) => {
    const [internalValue, setInternalValue] = useState(value ?? defaultValue ?? "");

    if (disabled) required = false;

    return (
        <Label htmlFor={id} required={required}>
            <Label.Title>{label}</Label.Title>
            <input
                className="rounded-md text-gray-800 disabled:bg-disabled focus:ring-primary focus:border-primary w-fit"
                type="text"
                id={id}
                name={name}
                {...value !== null
                    ? { value: value }
                    : { defaultValue: defaultValue }
                }
                onChange={onChangeFunction}
                disabled={disabled}
            />
        </Label>
    );
}

InputText.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    value: PropTypes.string,
    defaultValue: PropTypes.string,
    onChangeFunction: PropTypes.func,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputText;