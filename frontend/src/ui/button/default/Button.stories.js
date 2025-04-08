import Button from "./Button";
import { fn } from "@storybook/test";

export default {
    title: "Components/UI/Button",
    component: Button,
    tags: ["autodocs"],
    parameters: { layout: "centered" },
    args: {
        label: "Button",
        onClick: fn() }
}

export const Primary = {
    args: {
        primary: true
    }
}

export const Secondary = {}

export const Small = {
    args: {
        size: "small"
    }
}

export const Large = {
    args: {
        size: "large"
    }
}