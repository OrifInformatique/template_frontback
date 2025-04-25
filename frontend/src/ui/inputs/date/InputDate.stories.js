import InputDate from "./InputDate";

export default {
    title: "Components/UI/InputDate",
    component: InputDate,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: {
        label: "Date :"
     }
}

export const Default = {
    args: {
        id: "date-1",
        name: "date-1",
        required: true
    }
}

export const Disabled = {
    args: {
        id: "date-2",
        name: "date-2",
        disabled: true
    }
}