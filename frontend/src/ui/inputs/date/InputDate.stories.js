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

export const Uncontrolled = {
    args: {
        id: "date-3",
        name: "date-3",
        defaultValue: new Date().toISOString().slice(0, 10)
    }
}

export const Controlled = {
    args: {
        id: "date-3",
        name: "date-3",
        value: "",
        onChangeFunction: (date) => alert("Selected date is " + new Date(date).toLocaleDateString())
    }
}