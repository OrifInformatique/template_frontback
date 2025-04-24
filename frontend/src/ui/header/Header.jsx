import React, { useState } from "react";
import PropTypes from "prop-types";

// UI elements
import Icon from "../icon/Icon";
import Logo from "../logo";
import UserMenu from "../user-menu/UserMenu";

const Header = ({ user = null, title, childElement = null }) => {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <header className="border-b shadow-sm py-8 px-6">
            <div className="relative flex justify-between items-center">
                <Logo />
                <h1 className="absolute text-4xl left-1/2 transform -translate-x-1/2 center">
                    {title}
                </h1>
                <div className="flex items-center gap-x-4">
                    {childElement}
                    <a
                        href="#"
                        onClick={(e) => {
                            e.preventDefault();
                            setIsOpen(prev => !prev);
                        }}
                    >
                        <Icon name="user" />
                    </a>
                </div>
                {isOpen && (
                    <UserMenu user={user} setIsOpen={setIsOpen} />
                )}
            </div>
        </header>
    );
}

Header.propTypes = {
    user: PropTypes.shape({
        name: PropTypes.string.isRequired,
        role: PropTypes.oneOf(["admin", "user"]).isRequired
    }),
    title: PropTypes.string.isRequired,
    childElement: PropTypes.element
}

export default Header;