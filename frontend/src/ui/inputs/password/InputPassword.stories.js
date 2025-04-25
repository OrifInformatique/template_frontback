import InputPassword from "./InputPassword";

export default {
    title: "Components/UI/InputPassword",
    component: InputPassword,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: { label: "Mot de passe :" }
}

export const Default = {
    args: {
        id: "password-1",
        name: "password-1",
        placeholder: "Min. 8 caractères",
        required: true
    }
}

export const Disabled = {
    args: {
        disabled: true
    }
}