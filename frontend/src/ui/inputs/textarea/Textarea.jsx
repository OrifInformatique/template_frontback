import React from "react";
import PropTypes from "prop-types";

const Textarea = ({ name, id, text = "", ...props }) => {
    return (
        <textarea
            name={name}
            id={id}
            {...props}
        >
            {text}
        </textarea>
    );
}

Textarea.propTypes = {
    name: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    text: PropTypes.string
}

export default Textarea;