import React from "react";
import PropTypes from "prop-types";

const InputHidden = ({
    id,
    name,
    value = null
}) => {
    return (
        <input id={id} name={name} type="hidden" value={value} />
    );
}

InputHidden.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    value: PropTypes.string
}

export default InputHidden;