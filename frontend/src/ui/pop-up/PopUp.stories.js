import React, { useState } from "react";
import PopUp from "./PopUp";
import Button from "../buttons/default/Button";

export default {
  title: "Components/UI/PopUp",
  component: PopUp,
  tags: ["autodocs"],
  parameters: {
      layout: "fullscreen"
    }
}
export const DialogueSimple = () => {
  const [open, setOpen] = useState(false);

  return (
    <>
      <Button onClick={() => setOpen(true)} label="Ouvrir pop-up" variant="primary" />

      {open && (
        <PopUp
          title="Titre"
          description="Texte de description"
          onClose={() => setOpen(false)} 
        >
  <Button label="Oui" onClick={() => console.log("Oui cliqué")} variant="primary" />
  <Button label="Non" onClick={() => setOpen(false)} variant="danger" />
        </PopUp>
      )}
    </>
  );
};
