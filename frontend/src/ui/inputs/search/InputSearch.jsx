import React from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputSearch = ({ id, label, disabled = false }) => {
    return (
        <Label htmlFor={id} className="flex gap-2 items-center">
            <span className="text-primary font-medium">{label}</span>
            <input
                type="search"
                id={id}
                disabled={disabled}
            />
        </Label>
    );
}

InputSearch.propTypes = {
    
}

export default InputSearch;