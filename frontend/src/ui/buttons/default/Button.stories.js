import Button from "./Button";
import { fn } from "@storybook/test";

export default {
    title: "Components/UI/Button",
    component: Button,
    tags: ["autodocs"],
    parameters: { layout: "centered" },
    args: {
        onClick: () => alert("Button clicked!")
    }
}

export const Primary = {
    args: {
        variant: "primary",
        icon: "settings",
        label: "Settings"
    }
}

export const Secondary = {
    args: {
        variant: "secondary",
        label: "Save changes"
    }
}

export const Tertiary = {
    args: {
        label: "Cancel",
        icon: "restore"
    }
}

export const Danger = {
    args: {
        variant: "danger",
        icon: "cross"
    }
}

export const Small = {
    args: {
        size: "small",
        label: "Button"
    }
}

export const Large = {
    args: {
        size: "large",
        label: "Button"
    }
}