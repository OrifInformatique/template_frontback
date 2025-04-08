import React from "react";
import PropTypes from "prop-types";

const ScrollToTopButton = ({ onClick }) => {
    return (
        <button
            onClick={onClick}
            className="fixed bottom-16 right-8 p-3 bg-primary bg-opacity-80 rounded-full transition duration-300 hover:bg-opacity-100 hover:scale-105"
        >
            <svg className="size-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 10.5 12 3m0 0 7.5 7.5M12 3v18" />
            </svg>
        </button>
    );
}

ScrollToTopButton.propTypes = {
    onClick: PropTypes.func.isRequired
}

export default ScrollToTopButton;