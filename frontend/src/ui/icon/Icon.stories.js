import Icon from "./Icon";

export default {
    title: "Components/UI/Icon",
    component: Icon,
    tags: ["autodocs"],
    parameters: { layout: "centered" },
    args: {
        color: "black"
    }
}

export const AllIcons = (args) => (
    <div className="flex flex-wrap justify-center gap-4">
        <Icon {...args} name="home" />
        <Icon {...args} name="user" />
        <Icon {...args} name="edit" />
        <Icon {...args} name="delete" />
        <Icon {...args} name="duplicate" />
        <Icon {...args} name="restore" />
        <Icon {...args} name="history" />
        <Icon {...args} name="info" />
        <Icon {...args} name="check" />
        <Icon {...args} name="cross" />
        <Icon {...args} name="plus" />
        <Icon {...args} name="export" />
        <Icon {...args} name="camera" />
        <Icon {...args} name="filter" />
        <Icon {...args} name="settings" />
        <Icon {...args} name="burger" />
        <Icon {...args} name="meatballs" />
        <Icon {...args} name="kebab" />
        <Icon {...args} name="arrow-down" />
        <Icon {...args} name="arrow-left" />
        <Icon {...args} name="arrow-up" />
        <Icon {...args} name="arrow-right" />
        <Icon {...args} name="calendar" />
        <Icon {...args} name="login" />
        <Icon {...args} name="logout" />
        <Icon {...args} name="eye" />
        <Icon {...args} name="eye-slash" />
        <Icon {...args} name="magnifying-glass" />
        <Icon {...args} name="chevron-down" />
        <Icon {...args} name="chevron-left" />
        <Icon {...args} name="chevron-up" />
        <Icon {...args} name="chevron-right" />
        <Icon {...args} name="chevron-double-left" />
        <Icon {...args} name="chevron-double-right" />
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