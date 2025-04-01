import Icon from "./Icon";

export default {
    title: "Components/Icon",
    component: Icon,
    tags: ["autodocs"],
    parameters: { layout: "centered" },
    args: {
        color: "black",
        size: 12
    }
}

export const AllIcons = (args) => (
    <div className="flex gap-4">
        <Icon {...args} name="home" />
        <Icon {...args} name="user" />
        <Icon {...args} name="edit" />
        <Icon {...args} name="delete" />
    </div>
);