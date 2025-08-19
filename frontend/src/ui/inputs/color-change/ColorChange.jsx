import React, { useState } from "react";
import PropTypes from "prop-types";
import Label from "../../label/Label";

const ColorChange = ({
  label = "Couleur",
  defaultColor = "#005ba9",
  onChange,
  required = false, 
}) => {
  const [colorValue, setColorValue] = useState(defaultColor);
  const [hexInputValue, setHexInputValue] = useState(defaultColor.replace("#", ""));

  const handleColorChange = (e) => {
    const val = e.target.value;
    setColorValue(val);
    setHexInputValue(val.replace("#", ""));
    onChange && onChange(val);
  };

  const handleHexColorChange = (e) => {
    const input = e.target.value;
    setHexInputValue(input);
    const fullHex = `#${input}`;
    if (/^#[0-9A-Fa-f]{6}$/.test(fullHex)) {
      setColorValue(fullHex);
      onChange && onChange(fullHex);
    }
  };

  return (
    <div className="flex flex-col space-y-2 max-w-full mx-auto">
      <Label required={required}>
        <Label.Title>{label}</Label.Title>
      </Label>

      <div className="flex items-center space-x-2">
        <input
          type="color"
          value={colorValue}
          onChange={handleColorChange}
          className="w-10 h-10 rounded-full cursor-pointer focus:outline-none focus:ring-0"
        />
        <div className="flex items-center outline-none rounded px-0.5">
          <span className="text-black-500 text-bold px-1">#</span>
          <input
            type="text"
            value={hexInputValue}
            onChange={handleHexColorChange}
            maxLength="6"
            className="text-center outline-none px-1 py-0.5 w-20"
          />
        </div>
      </div>
    </div>
  );
};

ColorChange.propTypes = {
  label: PropTypes.string,
  defaultColor: PropTypes.string,
  onChange: PropTypes.func,
  required: PropTypes.bool  
};

export default ColorChange;
