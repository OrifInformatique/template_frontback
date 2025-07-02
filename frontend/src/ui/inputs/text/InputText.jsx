import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputText = ({ id, name, label, disabled = false, required = false }) => {
    if (disabled) required = false;

    return (
        <Label htmlFor={id} required>
            <Label.Title>{label}</Label.Title>
            <input
                className="rounded-md text-gray-800 disabled:bg-disabled focus:ring-primary focus:border-primary w-fit"
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