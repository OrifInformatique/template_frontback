import React from "react";
import PropTypes from "prop-types";
import Icon from "../icon/Icon";
import classNames from "classnames";

const PopUp = ({
  title,
  description,
  onClose,
  children,
  variant = "default" // "default" (desktop) ou "fullscreen" (mobile)
}) => {
  const isFullscreen = variant === "fullscreen";

  return (
    <div
      className={classNames(
        "fixed inset-0 z-50",
        {
          "flex items-center justify-center bg-black bg-opacity-50": !isFullscreen,
          "bg-white p-4 overflow-auto": isFullscreen
        }
      )}
    >
      <div
        className={classNames(
          "relative bg-white",
          {
            "w-full max-w-lg rounded-md shadow-lg p-6": !isFullscreen,
            "w-full h-full": isFullscreen
          }
        )}
      >
        {/* Header */}
        <div
          className={classNames(
            {
              "flex justify-between items-start mb-4": !isFullscreen,
              "flex justify-end items-center mb-4": isFullscreen
            }
          )}
        >
          {!isFullscreen && title && (
            <h2 className="text-xl font-semibold text-primary">{title}</h2>
          )}

          <button
            type="button"
            onClick={onClose}
            className="p-1 z-50 bg-transparent hover:bg-gray-100 rounded"
          >
            <Icon name="cross" size={6} color="black" />
          </button>
        </div>

        {/* Title & Description pour Fullscreen */}
        {isFullscreen && title && (
          <h2 className="text-lg font-semibold text-primary mb-2">{title}</h2>
        )}

        {description && (
          <p className="text-sm text-gray-600 mb-4">{description}</p>
        )}

        {/* Contenu */}
        <div className={isFullscreen ? "" : "flex justify-end gap-2 mt-4"}>
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
  variant: PropTypes.oneOf(["default", "fullscreen"])
};

export default PopUp;
