import { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const InputCheckbox = ({
    id,
    name,
    label,
    onChange = () => {},
    defaultChecked = false,
    disabled = false,
    required = false
}) => {
    const [isChecked, setIsChecked] = useState(defaultChecked);

    if (disabled && !defaultChecked) required = false;

    const handleCheckboxChange = (event) => {
        setIsChecked(event.target.checked);
    }

    return (
        <Label htmlFor={id} className="flex gap-2 items-center">
            <input
                type="checkbox"
                id={id}
                name={name}
                checked={isChecked}
                onChange={onChange}
                disabled={disabled}
                required={required}
            />
            <span>{label}</span>
        </Label>
    );
}

InputCheckbox.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    onChange: PropTypes.func,
    defaultChecked: PropTypes.bool,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputCheckbox;