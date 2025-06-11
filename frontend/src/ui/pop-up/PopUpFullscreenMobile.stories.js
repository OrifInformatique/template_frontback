import React, { useState } from "react";
import PopUpFullscreenMobile from "./PopUpFullscreenMobile";
import Button from "../buttons/default/Button";
import Title from "../title";


export default {
  title: "Components/UI/PopUpFullscreenMobile",
  component: PopUpFullscreenMobile,
};

export const FullscreenDemo = () => {
  const [open, setOpen] = useState(false);

  return (
    <>
      <Button
        label="Ouvrir plein écran"
        variant="primary"
        onClick={() => setOpen(true)}
      />

      {open && (
        <PopUpFullscreenMobile onClose={() => setOpen(false)}>
          <div className="space-y-4">
            <Title className="text-primary">Titre en plein écran</Title>
            <p className="text-sm text-center text-gray-600">Voici un texte de démonstration.</p>
          </div>
        </PopUpFullscreenMobile>
      )}
    </>
  );
};
