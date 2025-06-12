import React from "react";
import PropTypes from "prop-types";
import Icon from "../icon/Icon";
import classNames from "classnames";

const PopUp = ({ title, description, onClose, children }) => {
  return (
    <div className="fixed inset-0 z-50 bg-white sm:bg-black sm:bg-opacity-50 flex sm:items-center sm:justify-center overflow-auto">
      <div className="relative bg-white w-full h-full sm:h-auto sm:max-w-lg sm:rounded-md sm:shadow-lg p-4 sm:p-6">
        {/* Header */}
        <div className="flex items-center justify-end sm:justify-between sm:items-start mb-4">
          {/* Titre (desktop) */}
          <h2 className="hidden sm:block text-xl font-semibold text-primary">
            {title}
          </h2>

          <button
            type="button"
            onClick={onClose}
            className="p-1 z-50 bg-transparent hover:bg-gray-100 rounded"
          >
            <Icon name="cross" size={6} color="black" />
          </button>
        </div>

        {/* Titre (mobile) */}
        {title && (
          <h2 className="block sm:hidden text-lg font-semibold text-primary mb-2">
            {title}
          </h2>
        )}

        {description && (
          <p className="text-sm text-gray-600 mb-4">{description}</p>
        )}

        {/* Contenu enfants */}
        <div className="flex flex-col sm:flex-row sm:justify-end gap-2 mt-4">
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
  children: PropTypes.node,
};

export default PopUp;
