import { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputCheckbox = ({
    id,
    name,
    label,
    checked = null,
    defaultChecked = null,
    onChangeFunction = null,
    disabled = false,
    required = false
}) => {
    if (disabled && !defaultChecked) required = false;

    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <input
                type="checkbox"
                id={id}
                name={name}
                {...checked !== null
                    ? { checked: checked }
                    : { defaultChecked: defaultChecked }
                }
                onChange={onChangeFunction}
                disabled={disabled}
                required={required}
            />
            <span>{label}</span>
        </label>
    );
}

InputCheckbox.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    checked: PropTypes.bool,
    defaultChecked: PropTypes.bool,
    onChangeFunction: PropTypes.func,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputCheckbox;
