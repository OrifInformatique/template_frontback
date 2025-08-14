import InputCheckbox from "./InputCheckbox";

export default {
  title: "Components/UI/InputCheckbox",
  component: InputCheckbox,
  tags: ["autodocs"],
  layout: "fullscreen",
};

const baseOptions = [
  { id: "option-1", name: "choices", label: "Option 1" },
  { id: "option-2", name: "choices", label: "Option 2", defaultChecked: true },
  { id: "option-3", name: "choices", label: "Option 3" },
  { id: "option-4", name: "choices", label: "Option 4", disabled: true },
];

export const Default = {
  args: {
    options: baseOptions,
    required: false,
    onChange: (selectedIds) => console.log("Sélection :", selectedIds),
  },
};

export const AllDisabled = {
  args: {
    options: baseOptions,
    allDisabled: true,
    required: true,
    onChange: (selectedIds) => console.log("Sélection (disabled):", selectedIds),
  },
};

export const LabelOnLeft = {
  args: {
    name: "choices-left",
    options: baseOptions.map((opt) => ({ ...opt, labelPosition: "left" })),
    required: true,
    onChange: (selectedId) => console.log("Selected (left):", selectedId),
  },
};
