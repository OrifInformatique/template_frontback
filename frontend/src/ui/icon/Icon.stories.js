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

export const AllSizes = (args) => (
    <div className="flex items-center gap-4">
        <Icon {...args} size={4} />
        <Icon {...args} size={6} />
        <Icon {...args} size={8} />
        <Icon {...args} size={10} />
        <Icon {...args} size={12} />
        <Icon {...args} size={16} />
        <Icon {...args} size={20} />
        <Icon {...args} size={24} />
    </div>
);