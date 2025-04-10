import InputCheckbox from "./InputCheckbox";
import { fn } from "@storybook/test";

export default {
    title: "Components/UI/InputCheckbox",
    component: InputCheckbox,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: {
        label: "Checkbox",
        onChange: fn()
    }
}

export const Default = {}