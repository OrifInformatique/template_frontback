import React from "react";
import PropTypes from "prop-types";

const InputSearch = ({ id, label, disabled = false }) => {
    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <span className="text-primary font-medium">{label}</span>
            <input
                type="search"
                id={id}
                disabled={disabled}
            />
        </label>
    );
}

InputSearch.propTypes = {
    
}

export default InputSearch;