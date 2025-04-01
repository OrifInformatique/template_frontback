import Button from "./Button";
import { fn } from "@storybook/test";

export default {
    title: "Component/Button",
    component: Button,
    tags: ["autodocs"],
    parameters: { layout: "centered" },
    args: { onClick: fn() }
}

export const Primary = {}

export const Secondary = {
    args: {
        primary: false
    }
}

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