import React from "react";


import clsx from "clsx";

/**
 * UI component to display a textarea field.
 *
 * @param {string} name Name of the file input. Required.
 *
 * @param {string} [placeholder=null] Indicative text displayed in the input when empty. Null by default.
 *
 * @param {string} [value=null] Value of the field. Null by default. \
 * If provided, it means you want to control the input, and have to provide a function to update it (onChangeFunction prop).
 *
 * @param {string} [defaultValue=null] Default value of the input. Null by default. \
 * Only applies on uncontrolled inputs.
 *
 * @param {number} [rows=null] Number of visible rows by default. Null by default.
 *
 * @param {number} [cols=null] Number of visible columns (width of average char) by default. Null by default.
 *
 * @param {boolean} [resizeX=true] Decide whether to allow horizontal resizing. True by default.
 *
 * @param {boolean} [resizeY=true] Decide whether to allow vertical resizing. True by default.
 *
 * @param {boolean} [readonly=false] Decide whether the input is not editable. False by default.
 *
 * @param {boolean} [disabled=false] Decide whether the input is disabled. False by default.
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
const Textarea = ({
    name,
    placeholder = null,
    value = null,
    defaultValue = null,
    rows = null,
    cols = null,
    resizeX = true,
    resizeY = true,
    readonly = false,
    disabled = false,
    onChangeFunction = null,
    className
}) =>
{
    let resizeClass = "";

    switch(true)
    {
        case resizeX && resizeY:
            resizeClass = "resize"
            break;

        case !resizeX && resizeY:
            resizeClass = "resize-y"
            break;

        case resizeX && !resizeY:
            resizeClass = "resize-x"
            break;

        case !resizeX && !resizeY:
            resizeClass = "resize-none"
            break;
    }

    return (
        <>
            <textarea
                id={name}
                name={name}
                placeholder={placeholder}
                {...value !== null
                    ? { value: value }
                    : { defaultValue: defaultValue }
                }
                rows={rows}
                cols={cols}
                readOnly={readonly}
                disabled={disabled}
                onChange={onChangeFunction}
                className={clsx(
                    "rounded-md w-full",
                    resizeClass,
                    disabled ? "bg-stone-300 cursor-not-allowed" : "bg-background",
                    className
                )}
            />
        </>
    )
}

export default Textarea;
