import InputRadio from "./InputRadio";
import Label from "../../label/Label";

export default {
    title: "Components/UI/InputRadio",
    component: InputRadio,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: { label: "Radio" }
}

export const Default = {}

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