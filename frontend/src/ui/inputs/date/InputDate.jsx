import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputDate = ({
    id,
    name,
    label,
    value = null,
    defaultValue = null,
    onChangeFunction = null,
    disabled = false,
    required = false
}) => {
    if (disabled) required = false;

    return (
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
            <div>
                {required && <span className="text-red-700">* </span>}
                <span className="text-primary font-medium">{label}</span>
            </div>
            <input
                className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary"
                type="date"
                id={id}
                name={name}
                {...value !== null
                    ? { value: value }
                    : { defaultValue: defaultValue }
                }
                onChange={onChangeFunction}
                disabled={disabled}
                required={required}
            />
        </label>
    );
}

InputDate.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    value: PropTypes.string,
    defaultValue: PropTypes.string,
    onChangeFunction: PropTypes.func,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputDate;
