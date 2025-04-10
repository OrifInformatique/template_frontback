import React from "react";
import PropTypes from "prop-types";

const InputSubmit = ({ id, disabled = false }) => {
    return (
        <input
            id={id}
            type="submit"
            disabled={disabled}
        />
    );
}

InputSubmit.propTypes = {
    
}

export default InputSubmit;