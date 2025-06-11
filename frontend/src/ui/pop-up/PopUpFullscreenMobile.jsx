import React from "react";
import PropTypes from "prop-types";
import Icon from "../icon/Icon";

const PopUpFullscreenMobile = ({ title, description, onClose, children }) => {
    return (
        <div className="fixed inset-0 z-50 bg-white p-4 overflow-auto">
            <div className="flex justify-end items-center mb-4">
                {title && (
                    <h2 className="text-lg font-semibold">{title}</h2>
                )}
                <button onClick={onClose}>
                    <Icon name="cross" size={6} color="black" />
                </button>
            </div>
            {description && (
                <p className="text-sm text-gray-600 mb-4">{description}</p>
            )}
            <div>{children}</div>
        </div>
    );
};

PopUpFullscreenMobile.propTypes = {
    title: PropTypes.string,
    description: PropTypes.string,
    onClose: PropTypes.func.isRequired,
    children: PropTypes.node
};

export default PopUpFullscreenMobile;
