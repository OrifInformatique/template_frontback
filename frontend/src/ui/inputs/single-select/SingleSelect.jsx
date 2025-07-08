import React from "react";

import clsx from "clsx";

/**
 * UI component to select one or more options, in a dropdown.
 *
 * @param {string} name Name of the file input. Required.
 *
 * @param {string[]} [options=[]] Options of the input. Empty array by default.
 *
 * @param {string} [selectedValues=null] Value of the field. Null by default. \
 * If provided, it means you want to control the input, and have to provide a function to update it (onChangeFunction prop).
 *
 * @param {string} [defaultValue=null] Default value of the input. Null by default. \
 * Only applies on uncontrolled inputs.
 *
 * @param {boolean} [disabled=false] Decide whether the field is disabled. False by default. \
 * If options aren't provided or is empty, the input will also be disabled.
 *
 * @param {Function} [onChangeFunction=null] Function to call when the field is updated. Null by default.
 *
 * @param {string[]} [errors=[]] Invalid value errors for this field. Empty array by default.
 *
 * @param {string} [className=null] Additional and specific styles for the button. Null by default.
 *
 * @returns {JSX.Element}
 *
 */
const SingleSelect = ({
    name,
    options = [],
    selectedValue = null,
    defaultValue = null,
    disabled = false,
    onChangeFunction = null,
    errors = [],
    className = null
}) =>
{
    if(!name)
    {
        console.error("SingleSelect must have a name.");
        return;
    }

    const isDisabled = disabled || options.length < 1;

    /**
     * Calls the onChangeFunction if exists.
     *
     * @param {Event} event
     *
     * @returns {void}
     *
     */
    const handleSingleSelect = (event) =>
    {
        if(onChangeFunction)
            onChangeFunction(event.target.value);
    }

    return (
        <>
            <select
                id={name}
                name={name}
                {...selectedValue !== null
                    ? { value: selectedValue }
                    : { defaultValue: defaultValue }
                }
                disabled={isDisabled}
                onChange={handleSingleSelect}
                className={clsx(
                    "rounded-md w-full",
                    isDisabled ? "bg-stone-300 cursor-not-allowed" : "bg-background",
                    errors.length > 0 && "border-2 border-solid border-red-500",
                    className
                )}
            >
                {options.map(option => (
                    <option
                        key={option.value}
                        value={option.value}
                    >
                        {option.label}
                    </option>
                ))}
            </select>
        </>
    )
}

export default SingleSelect;