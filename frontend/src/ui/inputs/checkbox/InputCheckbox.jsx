import { useState } from "react";
import PropTypes from "prop-types";

const InputCheckbox = ({
    id,
    name,
    label,
    onChangeFunction = null,
    defaultChecked = false,
    checked = null,
    disabled = false,
    required = false
}) => {
    // Internal state of the input
    const [isChecked, setIsChecked] = useState(checked ?? defaultChecked ?? false);

    if (disabled && !defaultChecked) required = false;

    const handleCheckboxChange = (event) => {
        if(onChangeFunction)
            onChangeFunction(event.target.checked);

        setIsChecked(event.target.checked);
    }

    return (
        <label htmlFor={id} className="flex gap-2 items-center">
            <input
                type="checkbox"
                id={id}
                name={name}
                checked={isChecked}
                onChange={handleCheckboxChange}
                disabled={disabled}
                required={required}
            />
            <span>{label}</span>
        </label>
    );
}

InputCheckbox.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    onChangeFunction: PropTypes.func,
    defaultChecked: PropTypes.bool,
    checked: PropTypes.bool,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputCheckbox;