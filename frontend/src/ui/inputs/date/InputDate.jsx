import React, { useState } from "react";
import PropTypes from "prop-types";

const InputDate = ({ id, name, label, disabled = false, required = false }) => {
    const [selectedDate, setSelectedDate] = useState("");

    const handleDateChange = (event) => {
        setSelectedDate(event.target.value);
    }

    return (
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
            <span>{label}</span>
            <input
                id={id}
                name={name}
                type="date"
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