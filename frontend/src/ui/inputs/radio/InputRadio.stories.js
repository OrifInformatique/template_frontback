import InputRadio from "./InputRadio";

export default {
  title: "Components/UI/InputRadio",
  component: InputRadio,
  tags: ["autodocs"],
  layout: "fullscreen",
};

const baseOptions = [
  { id: "radio-1", label: "Option 1", defaultChecked: true },
  { id: "radio-2", label: "Option 2" },
  { id: "radio-3", label: "Option 3" },
  { id: "radio-4", label: "Option 4", disabled: true },
];

export const Default = {
  args: {
    name: "choices",
    options: baseOptions,
    required: true,
    onChange: (selectedId) => console.log("Selected radio:", selectedId),
  },
};

export const AllDisabled = {
  args: {
    name: "choices-disabled",
    options: baseOptions,
    disabledAll: true,
    required: false,
    onChange: (selectedId) => console.log("Selected radio (disabled):", selectedId),
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
