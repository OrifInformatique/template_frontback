import InputEmail from "./InputEmail";

export default {
    title: "Components/UI/InputEmail",
    component: InputEmail,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: { label: "Email :" }
}

export const Default = {
    args: {
        id: "email-1",
        name: "email-1",
        placeholder: "email@exemple.com",
        required: true
    }
}

export const Disabled = {
    args: {
        id: "email-2",
        name: "email-2",
        disabled: true,
    }
}