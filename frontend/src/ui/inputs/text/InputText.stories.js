import InputText from "./InputText";

export default {
    title: "Components/UI/InputText",
    component: InputText,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: { label: "Texte :" }
}

export const Default = {
    args: {
        id: "text-1",
        name: "text-1",
        required: true
    }
}

export const Disabled = {
    args: {
        id: "text-2",
        name: "text-2",
        disabled: true
    }
}