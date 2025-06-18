import InputCheckbox from "./InputCheckbox";
import { fn } from "@storybook/test";

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