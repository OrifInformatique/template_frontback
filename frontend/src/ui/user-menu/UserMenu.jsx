import React, { useRef, useEffect } from "react";
import PropTypes from "prop-types";

import Button from "../buttons/default/Button";

const UserMenu = ({
    user = null,
    setIsOpen,
    onLogin = () => {},
    onLogout = () => {}
}) => {
    const ref = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (ref.current && !ref.current.contains(event.target)) {
                setIsOpen(false);
            }
        }

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        }
    }, [setIsOpen]);

    return (
        <div ref={ref} className="absolute flex flex-col items-end gap-2 mt-4 right-2 top-full border bg-gray-100 p-4">
            {user ? (<>
                <span>Bonjour, <b>{user.name}</b> !</span>
                <ul className="flex flex-col items-end text-primary">
                    {user.role === "admin" && <li><a href="/">Administration</a></li>}
                    <li><a href="/">Changer de mot de passe</a></li>
                </ul>
                <Button label="Logout" icon="logout" onClick={onLogout} />
            </>) : (<>
                <p>Vous n'êtes pas connecté</p>
                <Button variant="primary" label="Login" icon="login" onClick={onLogin} />
            </>)}
        </div>
    );
}

UserMenu.propTypes = {
    user: PropTypes.shape({
        name: PropTypes.string.isRequired,
        role: PropTypes.oneOf(["admin", "user"]).isRequired
    }),
    setIsOpen: PropTypes.func.isRequired,
    onLogin: PropTypes.func,
    onLogout: PropTypes.func
}

export default UserMenu;