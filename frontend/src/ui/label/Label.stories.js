import React from "react";
import Label from "./Label";

export default {
  title: "Components/UI/Label",
  component: Label,
  tags: ["autodocs"],
  parameters: {
    layout: "fullscreen",
  },
};

export const Required = () => (
  <Label htmlFor="file" required>
    Label
  </Label>
);

export const NotRequired = () => (
  <Label htmlFor="file">
    Label
  </Label>
);

export const InlineRight = () => (
  <Label htmlFor="radio-inline-right" inlineRight>
    <input type="radio" id="radio-inline-right" />
    <span>Texte à droite</span>
  </Label>
);

export const InlineLeft = () => (
  <Label htmlFor="radio-inline-left" inlineLeft>
    <input type="radio" id="radio-inline-left" />
    <span>Texte à gauche</span>
  </Label>
);