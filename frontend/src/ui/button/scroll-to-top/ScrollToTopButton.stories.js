import { fn } from "@storybook/test";
import ScrollToTopButton from "./ScrollToTopButton";

export default {
    title: "Components/ScrollToTopButton",
    component: ScrollToTopButton,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: {
        onClick: fn()
    }
}

export const Default = {}