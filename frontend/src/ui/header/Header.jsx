import React from "react";
import PropTypes from "prop-types";

import Logo from "../logo";
import Button from "../button/Button";

const Header = ({ user = null, title, onLogin, onLogout }) => {
    return (
        <header className="border-b shadow-sm">
            <div className="flex justify-center items-center gap-4 py-8 px-4">
                <Logo className="mr-auto" />
                <h1 className="text-4xl">{title}</h1>
                <div className="ml-auto">
                    {user ? (
                        <Button
                            primary={false}
                            label="Logout"
                            size="medium"
                            onClick={onLogout}
                        />
                    ) : (
                        <Button
                            primary={true}
                            label="Login"
                            size="medium"
                            onClick={onLogin}
                        />
                    )}
                </div>
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="size-12 stroke-1">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
                </svg>
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