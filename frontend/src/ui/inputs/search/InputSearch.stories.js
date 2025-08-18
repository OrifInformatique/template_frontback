import InputSearch from "./InputSearch";

export default {
  title: "Components/UI/InputSearch",
  component: InputSearch,
  tags: ["autodocs"],
  layout: "fullscreen",
  args: {
    id: "search-1",
    label: "Rechercher",
  },
};

export const Default = {
  args: {
    required: true, 
  },
};

export const Disabled = {
  args: {
    disabled: true,
  },
};
