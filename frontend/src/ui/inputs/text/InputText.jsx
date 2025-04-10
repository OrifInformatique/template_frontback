import React from "react";
import PropTypes from "prop-types";

const InputText = ({ id, label, disabled = false }) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <span>{label}</span>
            <input
                id={id}
                type="text"
                disabled={disabled}
            />
        </label>
    );
}

InputText.propTypes = {

}

export default InputText;