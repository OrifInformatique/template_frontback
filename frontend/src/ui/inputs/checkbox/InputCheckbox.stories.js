import InputCheckbox from "./InputCheckbox";

export default {
  title: "Components/UI/InputCheckbox",
  component: InputCheckbox,
  tags: ["autodocs"],
  layout: "fullscreen",
  args: {
    label: "Checkbox",
    onChange: (e) => console.log("Changement :", e.target.checked)
  }
};

export const Default = {
  args: {
    id: "checkbox-1",
    name: "checkbox-1",
    required: true
  }
};

export const Disabled = {
  args: {
    id: "checkbox-2",
    name: "checkbox-2",
    defaultChecked: true,
    disabled: true
  }
};
