import React from "react";
import PropTypes from "prop-types";

const InputDate = ({ id, label, disabled = false }) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <span>{label}</span>
            <input
                id={id}
                type="date"
                disabled={disabled}
            />
        </label>
    );
}

InputDate.propTypes = {
    
}

export default InputDate;