import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputText = ({ id, name, label, disabled = false, required = false }) => {
    if (disabled) required = false;

    return (
        <Label htmlFor={id} required={required}>
            {label}
            <input
                className="rounded-md pr-10 disabled:bg-disabled focus:ring-primary focus:border-primary"
                type="text"
                id={id}
                name={name}
                disabled={disabled}
            />
        </Label>
    );
}

InputText.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputText;