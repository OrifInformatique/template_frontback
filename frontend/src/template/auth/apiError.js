/**
 * Message to show for a failed API request.
 *
 * The backend answers every error with the same body ({ status, error, message,
 * fieldErrors }): its message is already translated (Accept-Language), and
 * fieldErrors lists the invalid fields of a validation error.
 *
 * @param {Error} error the axios error
 * @param {string} fallback translated message to use when the backend gave none
 *        (network error, backend unreachable)
 * @returns {string} the message to show
 */
export const apiErrorMessage = (error, fallback) => {
    const data = error?.response?.data;
    let message = data?.message || fallback;
    if (data?.fieldErrors) {
        message += ": " + Object.entries(data.fieldErrors)
            .map(([field, fieldError]) => `${field} ${fieldError}`)
            .join(", ");
    }
    return message;
};
