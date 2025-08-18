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
        id: "password-2",
        name: "password-2",
        disabled: true,
        required: false
    }
}

export const Uncontrolled = {
    args: {
        id: "password-3",
        name: "password-3",
        defaultValue: "myDefaultPassword123",
        required: true
    }
}

export const Controlled = {
    args: {
        id: "password-4",
        name: "password-4",
        value: "",
        required: true,
        onChangeFunction: (password) => alert("The input contains \"" + password + "\".")
    }
}