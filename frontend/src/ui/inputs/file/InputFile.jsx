import React from "react";
import PropTypes from "prop-types";

const InputFile = ({ id, disabled = false }) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <input
                id={id}
                type="file"
                disabled={disabled}
            />
        </label>
    );
}

InputFile.propTypes = {
    
}

export default InputFile;