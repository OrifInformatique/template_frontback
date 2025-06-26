import InputCheckbox from "./InputCheckbox";
import { fn } from "@storybook/test";
import Label from "../../label/Label";

export default {
    title: "Components/UI/InputCheckbox",
    component: InputCheckbox,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: {
        label: "Checkbox",
        onChange: (e) => {
            
        }
    }
}

export const Default = {
    args: {
        id: "checkbox-1",
        name: "checkbox-1",
        required: true
    }
}

export const Disabled = {
    args: {
        id: "checkbox-2",
        name: "checkbox-2",
        defaultChecked: true,
        disabled: true
    }
}

export const InlineRight = () => (
    <Label htmlFor="checkbox-inline-right" inlineRight>
      <input type="checkbox" id="checkbox-inline-right" />
      <span>Texte à droite</span>
    </Label>
  );
  
  export const InlineLeft = () => (
    <Label htmlFor="checkbox-inline-left" inlineLeft>
      <input type="checkbox" id="checkbox-inline-left" />
      <span>Texte à gauche</span>
    </Label>
  );