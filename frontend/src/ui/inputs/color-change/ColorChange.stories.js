import React, { useState } from "react";
import ColorChange from "./ColorChange";
import InputText from "../text/InputText";

export default {
  title: "Components/UI/ColorChange",
  component: ColorChange,
  tags: ["autodocs"],
  parameters: {
    layout: "fullscreen"
  }
};

export const Exemple = () => {
  const [color, setColor] = useState("#005ba9");

  return (
    <div className="p-4 max-w-sm">
    <ColorChange
      label="Choisir une couleur"
      defaultColor={color}
      required={true} 
      onChange={(newColor) => setColor(newColor)}
      >
        <InputText
          id="example-text"
          name="example"
          label="Nom de la couleur"
          required
        />
      </ColorChange>
    </div>
  );
};
