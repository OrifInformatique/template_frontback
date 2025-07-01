import InputCheckbox from "./InputCheckbox";
import { fn } from "@storybook/test";
import Label from "../../label/Label";

export default {
    title: "Components/UI/InputCheckbox",
    component: InputCheckbox,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: {
        label: "Checkbox"
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
        disabled: true
    }
}

export const Uncontrolled = {
    args: {
        id: "checkbox-3",
        name: "checkbox-3",
        defaultChecked: true,
    }
}

export const Controlled = {
    args: {
        id: "checkbox-3",
        name: "checkbox-3",
        onChangeFunction: (value) => alert("Checkbox is " + (value ? "checked" : "unchecked")),
        checked: false
    }
}
