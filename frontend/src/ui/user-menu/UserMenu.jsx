import React, { useRef, useEffect } from "react";
import PropTypes from "prop-types";

const UserMenu = ({ user = null, setIsOpen }) => {
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
                <div>Bonjour, <b>{user.name}</b> !</div>
                <ul className="flex flex-col items-end text-primary">
                    <li><a href="/">Changer de mot de passe</a></li>
                    {user.role === "admin" && <li><a href="/">Administration</a></li>}
                </ul>
            </>) : (
                <ul>
                    <li>Vous n'êtes pas connecté</li>
                </ul>
            )}
        </div>
    );
}

UserMenu.propTypes = {
    user: PropTypes.shape({
        name: PropTypes.string.isRequired,
        role: PropTypes.oneOf(["admin", "user"]).isRequired
    }),
    isOpen: PropTypes.bool.isRequired,
    setIsOpen: PropTypes.func.isRequired
}

export default UserMenu;