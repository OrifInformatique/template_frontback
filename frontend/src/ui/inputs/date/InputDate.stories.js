import InputDate from "./InputDate";

export default {
  title: "Components/UI/InputDate",
  component: InputDate,
  tags: ["autodocs"],
  layout: "fullscreen",
  args: {
    label: "Date :"
  }
};

export const Default = {
  args: {
    id: "date-1",
    name: "date-1",
    required: true
  }
};

export const Disabled = {
  args: {
    id: "date-2",
    name: "date-2",
    disabled: true,
    required: false
  }
};

export const Uncontrolled = {
  args: {
    id: "date-uncontrolled",
    name: "date-uncontrolled",
    defaultValue: new Date().toISOString().slice(0, 10)
  }
};

export const Controlled = {
  args: {
    id: "date-controlled",
    name: "date-controlled",
    value: "",
    onChangeFunction: (e) => {
      const date = e.target.value;
      alert("Selected date is " + new Date(date).toLocaleDateString());
    }
  }
};
