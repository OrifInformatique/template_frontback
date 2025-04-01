import React from "react";
import PropTypes from "prop-types";

const Button = ({
    className = "", primary = false, label, size = "medium", ...props
}) => {
    const mode = primary ? "bg-primary text-white focus:bg-opacity-80 transition"
        : "border border-black border-opacity-60 focus:text-gray-600 transition";

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
            className={["font-medium rounded-full px-4 py-2",
                className, mode, buttonSize(size)].join(" ")}
            {...props}
        >
            {label}
        </button>
    );
}

Button.propTypes = {
    className: PropTypes.string,
    primary: PropTypes.bool,
    label: PropTypes.string.isRequired,
    size: PropTypes.oneOf(["small", "medium", "large"]),
    onClick: PropTypes.func
}

export default Button;