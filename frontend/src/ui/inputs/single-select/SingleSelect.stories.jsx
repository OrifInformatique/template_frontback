import SingleSelect from "./SingleSelect";

export default {
    title: "Components/UI/SingleSelect",
    component: SingleSelect,
    tags: ["autodocs"],
    layout: "fullscreen",
    args: {
        name: "singleselect",
            options: [{
                value: "option-1",
                label: "Option 1"
        },
        {
            value: "option-2",
            label: "This is a sample text"
        }]
    }
}

export const Default = {}