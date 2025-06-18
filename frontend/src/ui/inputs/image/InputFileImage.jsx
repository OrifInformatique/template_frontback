import React from "react";
import PropTypes from "prop-types";

const InputFileImage = ({ src, alt = "" }) => {
    return (
        <input type="image" src={src} alt={alt} />
    );
}

InputFileImage.propTypes = {
    src: PropTypes.string.isRequired,
    alt: PropTypes.string
}

export default InputFileImage;