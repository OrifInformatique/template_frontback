import { fn } from "@vitest/spy";
import UserMenu from "./UserMenu";
import Icon from "../icon/Icon";

export default {
    title: "Components/UI/UserMenu",
    component: UserMenu,
    tags: ["autodocs"],
    parameters: {
        layout: "fullscreen"
    }
}

export const LoggedIn = {
    render: (args) => (
        <div class="relative flex justify-end">
            <a
                href="#"
                onClick={(e) => {
                    e.preventDefault();
                    setIsOpen(prev => !prev);
                }}
            >
                <Icon name="user" />
            </a>
            <UserMenu {...args} />
        </div>
    ),
    args: {
        user: {
            name: "John Doe",
            role: "admin"
        },
        isOpen: true,
        setIsOpen: fn()
    }
}

export const LoggedOut = {
    render: (args) => (
        <div class="relative flex justify-end">
            <a
                href="#"
                onClick={(e) => {
                    e.preventDefault();
                    setIsOpen(prev => !prev);
                }}
            >
                <Icon name="user" />
            </a>
            <UserMenu {...args} />
        </div>
    ),
    args: {
        user: null,
        isOpen: true,
        setIsOpen: fn()
    }
}