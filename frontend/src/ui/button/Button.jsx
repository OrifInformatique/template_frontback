import React from "react";
import PropTypes from "prop-types";

const Button = ({ primary = true, label, size = "medium", ...props }) => {
    const mode = primary ? "bg-primary text-white"
        : "border border-black border-opacity-70";
    
    const buttonSize = (size) => {
        switch (size) {
            case "small":
                return "text-sm";

            case "medium":
                return "text-md";

            case "large":
                return "text-lg";

            default:
                return "text-md";
        }
    }

    return (
        <button
            className={["font-medium rounded-full min-w-24 min-h-10 px-4 py-2", mode, buttonSize(size)].join(" ")}
            {...props}
        >
            {label}
        </button>
    );
}

Button.propTypes = {
    primary: PropTypes.bool,
    label: PropTypes.string.isRequired,
    size: PropTypes.oneOf(["small", "medium", "large"]),
    onClick: PropTypes.func
}

export default Button;