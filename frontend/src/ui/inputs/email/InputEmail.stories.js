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
        required: false,
    }
}

export const Uncontrolled = {
    args: {
        id: "email-3",
        name: "email-3",
        defaultValue: "enter.email@here.com",
        required: true
    }
}

export const Controlled = {
    args: {
        id: "email-4",
        name: "email-4",
        value: "",
        required: true,
        onChangeFunction: (email) => alert("The input contains \"" + email + "\".")
    }
}