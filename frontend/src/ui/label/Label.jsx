import React from "react";
import PropTypes from "prop-types";
import clsx from "clsx";

const Label = ({ htmlFor, children, required = false, className = "" }) => {
  return (
    <label htmlFor={htmlFor} className={clsx("flex flex-col gap-1", className)}>
      {React.Children.map(children, (child, index) =>
        index === 0 && required ? (
          <div className="flex items-center gap-1">
            <span className="text-danger">*</span>
            {child}
          </div>
        ) : (
          child
        )
      )}
    </label>
  );
};

const LabelTitle = ({ children, unstyled = false, className = "" }) => (
  <span className={clsx(!unstyled && "text-primary font-medium", className)}>
    {children}
  </span>
);

LabelTitle.propTypes = {
  children: PropTypes.node.isRequired,
  unstyled: PropTypes.bool,
  className: PropTypes.string,
};

Label.Title = LabelTitle;

Label.propTypes = {
  htmlFor: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
  required: PropTypes.bool,
  className: PropTypes.string,
};

export default Label;
