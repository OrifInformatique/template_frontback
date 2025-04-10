import React from "react";
import PropTypes from "prop-types";

const InputNumber = ({ id, label, disabled = false }) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <span>{label}</span>
            <input
                id={id}
                type="number"
                disabled={disabled}
            />
        </label>
    );
}

InputNumber.propTypes = {
    
}

export default InputNumber;