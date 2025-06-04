import React, { useState } from "react";
import PropTypes from "prop-types";

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
        <label htmlFor={id} className="flex flex-col gap-2">
            <div>
                {required && <span className="text-red-700">* </span>}
                <span className="text-primary font-medium">{label}</span>
            </div>
            <input
                className="rounded-md pr-10 disabled:bg-disabled focus:ring-primary focus:border-primary"
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
        </label>
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