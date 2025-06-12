import React, { useState } from "react";
import PopUp from "./PopUp";
import Button from "../buttons/default/Button";
import Title from "../title";

export default {
  title: "Components/UI/PopUp",
  component: PopUp,
  tags: ["autodocs"],
  parameters: {
    layout: "fullscreen"
  }
};

export const DialogueDesktop = () => {
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
          <Button label="Accepter" onClick={() => console.log("Oui cliqué")} variant="primary" />
          <Button label="Non" onClick={() => setOpen(false)} variant="danger" />
        </PopUp>
      )}
    </>
  );
};

export const DialogueMobile = () => {
  const [open, setOpen] = useState(false);

  return (
    <>
      <Button
        label="Ouvrir plein écran"
        variant="primary"
        onClick={() => setOpen(true)}
      />

      {open && (
        <PopUp
          title=""
          description=""
          onClose={() => setOpen(false)}
        >
          <Title className="text-primary"></Title>
          <p>Appuier sur f12 pour permettre la visualisation Mobile
          Laisser par default pour une visualisation Desktop</p>
        </PopUp>
      )}
    </>
  );
};
