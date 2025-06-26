import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputRadio = ({ id, label, disabled = false }) => {
    return (
        <Label htmlFor={id} className="flex gap-2 items-center">
            <input
                type="radio"
                id={id}
                disabled={disabled}
            />
            <span>{label}</span>
        </Label>
    );
}

InputRadio.propTypes = {
    
}

export default InputRadio;