import React from "react";
import PropTypes from "prop-types";
import Icon from "../icon/Icon";

const PopUp = ({ title, description, onClose, children }) => {
  return (
    <div className="fixed inset-0 z-50 flex sm:items-center sm:justify-center bg-white sm:bg-black sm:bg-opacity-50 p-4 sm:p-0 overflow-auto">
      <div className="relative w-full sm:max-w-lg bg-white rounded-none sm:rounded-md shadow-none sm:shadow-lg p-6 sm:h-auto">
        {/* Header */}
        <div className="flex justify-between items-start mb-4">
          <h2 className="text-xl font-semibold text-primary">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            className="p-1 z-50 bg-transparent hover:bg-gray-100 rounded"
          >
            <Icon name="cross" size={6} color="black" />
          </button>
        </div>

        {/* Description */}
        {description && (
          <p className="text-sm text-gray-600 mb-4">{description}</p>
        )}

        {/* Contenu */}
        <div className="flex flex-col sm:flex-row sm:justify-end sm:gap-2 sm:mt-4">
          {children}
        </div>
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
