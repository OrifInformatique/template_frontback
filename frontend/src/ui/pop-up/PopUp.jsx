import React from "react";
import PropTypes from "prop-types";
import Icon from "../icon/Icon";

const PopUp = ({ title, description, onClose, children }) => {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
            <div className="relative w-full max-w-lg bg-white rounded-md shadow-lg p-6">
            <button
            type="button"
            onClick={onClose}
            className="absolute top-4 right-4 p-1 z-50 bg-transparent hover:bg-gray-100 rounded"
            >
            <Icon name="cross" size={6} color="black" />
            </button>

                {title && (
                    <h2 className="text-xl font-semibold mb-2">{title}</h2>
                )}
                {description && (
                    <p className="text-sm text-gray-600 mb-4">{description}</p>
                )}

                <div>{children}</div>
            </div>
        </div>
    );
};

PopUp.propTypes = {
    title: PropTypes.string,
    description: PropTypes.string,
    onClose: PropTypes.func.isRequired,
    children: PropTypes.node
};

export default PopUp;
