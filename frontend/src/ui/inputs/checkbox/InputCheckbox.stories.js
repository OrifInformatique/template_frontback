import InputCheckbox from "./InputCheckbox";

export default {
  title: "Components/UI/InputCheckbox",
  component: InputCheckbox,
  tags: ["autodocs"],
  layout: "fullscreen"
};

export const MultipleOptions = {
  args: {
    options: [
      { id: "option-1", name: "choices", label: "Option 1" },
      { id: "option-2", name: "choices", label: "Option 2", defaultChecked: true },
      { id: "option-3", name: "choices", label: "Option 3" },
      { id: "option-4", name: "choices", label: "Option 4", disabled: true }
    ],
    onChange: (selectedIds) => console.log("Sélection :", selectedIds)
  }
};
