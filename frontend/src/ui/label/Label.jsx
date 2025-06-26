import React from "react";
import PropTypes from "prop-types";
import clsx from "clsx";

const Label = ({
  htmlFor,
  children,
  required = false,
  inlineRight = false,
  inlineLeft = false,
  className = "",
}) => {
  const isInline = inlineLeft || inlineRight;

  const layoutClass = clsx(
    isInline
      ? "flex gap-2 items-center justify-start w-fit"
      : "inline-flex flex-col gap-1",
    inlineLeft && "flex-row-reverse",
    className
  );

  const childrenArray = React.Children.toArray(children);
  const labelContent = childrenArray[0];
  const inputContent = childrenArray[1];

  return (
    <label htmlFor={htmlFor} className={layoutClass}>
      {!isInline ? (
        <>
          <span className="text-primary font-medium">
            {required && <span className="text-danger">*</span>} {labelContent}
          </span>
          {inputContent}
        </>
      ) : (
        children
      )}
    </label>
  );
};

Label.propTypes = {
  htmlFor: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
  required: PropTypes.bool,
  inlineRight: PropTypes.bool,
  inlineLeft: PropTypes.bool,
  className: PropTypes.string,
};

export default Label;
