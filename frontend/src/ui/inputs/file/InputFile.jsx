import React, { useState } from "react";
import PropTypes from "prop-types";

const InputFile = ({
    id,
    name,
    label = null,
    accept = "",
    disabled = false,
    required = false
}) => {
    // Internal state of the input
    const [file, setFile] = useState(null);

    if (disabled) required = false;

    const handleFileChange = (event) => {
        const selectedFile = event.target.files[0];
        if (selectedFile) {
            setFile(selectedFile);
        } else {
            setFile(null);
        }
    }

    return (
        <label htmlFor={id} className="flex flex-col gap-2 items-start">
        <div>
            {required && <span className="text-red-700">* </span>}
            <span className="text-primary font-medium">{label}</span>
        </div>
            <input
                type="file"
                id={id}
                name={name}
                accept={accept}
                onChange={handleFileChange}
                disabled={disabled}
                required={required}
            />
        </label>
    );
}

InputFile.propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    label: PropTypes.string,
    accept: PropTypes.string,
    disabled: PropTypes.bool,
    required: PropTypes.bool
}

export default InputFile;