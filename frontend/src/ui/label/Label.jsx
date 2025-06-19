import React from "react";
import PropTypes from "prop-types";

const Label = ({ htmlFor, children, required = false }) => {
  return (
    <label htmlFor={htmlFor} className="text-primary font-medium">
      {required && <span className="text-danger">*</span>} {children}
    </label>
  );
};

Label.propTypes = {
  htmlFor: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
  required: PropTypes.bool,
};

export default Label;
