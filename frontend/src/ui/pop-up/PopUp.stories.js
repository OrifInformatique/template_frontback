import React, { useState } from "react";
import PopUp from "./PopUp";
import Button from "../buttons/default/Button";

export default {
  title: "Components/UI/PopUp",
  component: PopUp,
  tags: ["autodocs"],
  parameters: {
    layout: "Responsive"
  }
};

export const DialogueResponsive = () => {
  const [open, setOpen] = useState(false);

  return (
    <>
      <Button
        onClick={() => setOpen(true)}
        label="Ouvrir le PopUp"
        variant="primary"
      />

      {open && (
      <PopUp
        title="Titre"
        description="Press F12 to test responsive or use the tool within storybook"
        onClose={() => setOpen(false)}
      >
        <Button label="Accepter" onClick={() => console.log("Oui cliqué")} variant="primary" />
        <Button label="Annuler" onClick={() => setOpen(false)} variant="danger" />
      </PopUp>
      )}
    </>
  );
};
