import InputNumber from "./InputNumber";

export default {
    title: "Components/UI/InputNumber",
    component: InputNumber,
    tags: ["autodocs"],
    layout: "fullscreen"
}

export const LimitedValues = {
    args: {
        id: "number-1",
        name: "number-1",
        label: "Votre âge :",
        min: 0,
        max: 130,
        required: true
    }
}

export const Disabled = {
    args: {
        id: "number-2",
        name: "number-2",
        label: "Quantité :",
        disabled: true
    }
}