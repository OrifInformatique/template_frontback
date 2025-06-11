import React, { useState } from "react";
import PopUp from "./PopUp";
import Button from "../buttons/default/Button";

export default {
  title: "Components/UI/PopUp",
  component: PopUp,
};

export const DialogueSimple = () => {
  const [open, setOpen] = useState(false);

  return (
    <>
      <Button onClick={() => setOpen(true)} label="Ouvrir pop-up" variant="primary" />

      {open && (
        <PopUp
          title="Titre"
          description="Texte de description"
          onClose={() => setOpen(false)} // ⬅️ Ferme le pop-up
        >
          <p className="text-sm text-gray-700">Contenu du PopUp</p>
        </PopUp>
      )}
    </>
  );
};
