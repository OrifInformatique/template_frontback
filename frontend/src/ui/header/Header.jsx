import React from "react";
import PropTypes from "prop-types";

import Logo from "../logo";
import Button from "../button/Button";
import Icon from "../icon/Icon";

const Header = ({ user = null, title, onLogin, onLogout }) => {
    return (
        <header className="border-b shadow-sm py-8 px-6">
            <div className="flex items-center gap-4">
                <Logo />
                <h1 className="absolute left-1/2 transform -translate-x-1/2 center text-4xl">{title}</h1>
                {user ? (
                    <Button
                        primary={false}
                        label="Logout"
                        size="medium"
                        onClick={onLogout}
                        className="ml-auto"
                    />
                ) : (
                    <Button
                        primary={true}
                        label="Login"
                        size="medium"
                        onClick={onLogin}
                        className="ml-auto"
                    />
                )}
                <Icon name="user" />
            </div>
        </header>
    );
}

Header.propTypes = {
    user: PropTypes.shape({
        name: PropTypes.string.isRequired
    }),
    onLogin: PropTypes.func.isRequired,
    onLogout: PropTypes.func.isRequired
}

export default Header;