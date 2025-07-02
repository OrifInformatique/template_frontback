import React, { useEffect, useState } from "react";

import clsx from "clsx";

import Label from "../../label/Label";

/**
 * UI component to select one or more options, in a dropdown.
 *
 * @param {string} name Name of the input. Required.
 *
 * @param {string[]} [options=[]] Options of the input. Empty array by default.
 *
 * @param {string} [selectedValues=[]] Value of the date field. Empty array by default. \
 * If provided, it means you want to control the input, and have to provide a function to update it (onChangeFunction prop).
 *
 * @param {string} [defaultValues=[]] Default value of the date input. Empty array by default. \
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
const MultiSelect = ({
    name,
    options = [],
    selectedValues = [],
    defaultValues = [],
    disabled = false,
    onChangeFunction = null,
    errors = [],
    className = null
}) =>
{
    if(!name)
    {
        console.error("MultiSelect must have a name.");
        return;
    }

    const [selectedCount, setSelectedCount] = useState(0);
    const [selectedOptions, setSelectedOptions] = useState(defaultValues)
    const [isOpen, setIsOpen] = useState(false);

    const isDisabled = disabled || options.length < 1;

    /**
     * Updates the internal selected elements and calls the onChangeFunction if exists.
     *
     * @param {string} value The value being selected or unselected.
     *
     * @returns {void}
     *
     */
    const handleSelectedOptions = (value) =>
    {
        if(onChangeFunction)
            onChangeFunction((prev) =>
                prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
            );

        setSelectedOptions((prev) =>
            prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
        );
    };

    useEffect(() => setSelectedCount(selectedOptions.length), [selectedOptions]);

    return (
        <>
            <div className="relative">
                <div
                    onClick={() => { if(!isDisabled) setIsOpen((prev) => !prev) }}
                    className={clsx(
                        "rounded-md w-full h-fit px-4 py-2 border border-gray-500 text-center select-none hover:cursor-pointer",
                        isDisabled ? "bg-stone-300 cursor-not-allowed" : "bg-background",
                        errors.length > 0 && "border-2 border-solid border-red-500",
                        className
                    )}
                >
                    {selectedCount > 0 ? (
                        <p className={clsx(
                            isDisabled
                                ? "hover:cursor-not-allowed"
                                : "hover:cursor-pointer")
                        }>
                            {`${selectedCount} ${selectedCount > 1
                                ? "sélectionnés"
                                : "sélectionné"
                            }`}
                        </p>
                    ) : (
                        <p className={clsx(
                            isDisabled
                                ? "hover:cursor-not-allowed"
                                : "hover:cursor-pointer")
                        }>
                            Aucun sélectionné
                        </p>
                    )}
                </div>

                <div
                    id={`${name}-multiselect`}
                    className={`${!isOpen && "!hidden"} block absolute border border-blue min-w-max w-full py-2 space-y-2 rounded-md z-50 bg-white`}
                    onClick={(e) => e.stopPropagation()}
                >
                    {options.map((option, index) => (
                        <div
                            key={`${name}-${index}`}
                            className="space-x-2 w-full px-2 select-none"
                        >
                            <input
                                id={`${name}-${index}`}
                                name={`${name}-${index}`}
                                type="checkbox"
                                value={option}
                                {...(onChangeFunction !== null
                                    ? { checked: selectedValues.includes(option) }
                                    : { defaultChecked: defaultValues.includes(option) })
                                }
                                onChange={() => handleSelectedOptions(option)}
                                className="rounded-sm hover:cursor-pointer"
                            />

                            <Label forInput={`${name}-${index}`}>
                                {option}
                            </Label>
                        </div>
                    ))}
                </div>
            </div>
        </>
    )
}

export default MultiSelect;