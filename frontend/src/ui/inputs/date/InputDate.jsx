import React, { useState } from "react";
import PropTypes from "prop-types";

const InputDate = ({
    id,
    name,
    label,
    value = null,
    defaultValue = null,
    onChangeFunction = null,
    disabled = false,
    required = false
}) => {
    // Internal state to the input
    const [selectedDate, setSelectedDate] = useState(value ?? defaultValue ?? "");

    if (disabled) required = false;

    const handleDateChange = (event) => {
        if(onChangeFunction)
            onChangeFunction(event.target.value);

        setSelectedDate(event.target.value);
    }

    return (
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
            <div>
                {required && <span className="text-red-700">* </span>}
                <span className="text-primary font-medium">{label}</span>
            </div>
            <input
                className="rounded-md disabled:bg-disabled focus:ring-primary focus:border-primary"
                type="date"
                id={id}
                name={name}
                value={selectedDate}
                onChange={handleDateChange}
                disabled={disabled}
                required={required}
            />
        </label>
    );
}

InputDate.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputDate;