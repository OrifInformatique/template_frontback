import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next';
import { Button, InputPassword, InputText, MultiSelect  } from '@orif-informatique/react-components-library';

import { createUser, updateUser, getRoles } from "./api/api";
import { apiErrorMessage } from "../auth/apiError";

function UserForm({ user, onClose, onSaved }) {
const { t } = useTranslation('users');
const [firstName, setFirstName] = useState(user ? user.firstName : "");
const [lastName, setLastName] = useState(user ? user.lastName : "");
const [login, setLogin] = useState(user ? user.login : "");
const [password, setPassword] = useState("");
    const [userRoles, setUserRoles] = useState(user?.mainRole || "USER");
const [roles, setRoles] = useState([]);
const [appSpefRole, setAppSpefRole] = useState([])
const [userAppSpefRole, setUserAppSpefRole] = useState(user ? user.appSpecificRoles : [])
const [saveError, setSaveError] = useState("");
const roleName = []
const userRolesName = []

appSpefRole.map(r => roleName.push(r.name))
userAppSpefRole.map(r => userRolesName.push(r.name))



useEffect(() => {
    const fetchRoles = async () => {
        try {
            const mainRoles = await getRoles('main');
            const localRoles = await getRoles('local');
            setRoles(mainRoles);
            setAppSpefRole(localRoles);
        } catch (error) {
            console.error("Roles loading failed:", error);
            setSaveError(apiErrorMessage(error, t("roles_error", "The roles could not be loaded.")));
        }
    };

    fetchRoles();
}, [user]);

useEffect(() => {
    if (user) {
        setFirstName(user.firstName || "");
        setLastName(user.lastName || "");
        setLogin(user.login || "");
        setPassword(user.password || "");
            setUserRoles(user.mainRole || "USER");
        setUserAppSpefRole(user.appSpecificRoles || [])

        console.log("USER_APP_SPECIFIC_ROLES : " + user.appSpecificRoles)

        console.log("User data loaded into form:", { firstName, lastName, login, password, userRoles, userAppSpefRole });
    }
}, [user]);

const handleSaveError = (error) => {
    // Backend message, with the invalid fields of a validation error
    setSaveError(apiErrorMessage(error, t("save_error", "The user could not be saved.")));
    console.error("User save failed:", error.response?.data || error);
};

const handleSave = async () => {
    setSaveError("");

    try {
        if (user) {
            await updateUser(user.id, {
                "firstName" : firstName,
                "lastName" : lastName,
                "login" : login,
                "mainRole": userRoles,
                "appSpecificRoles" : userAppSpefRole
            });
        } else {
            await createUser({
                "firstName" : firstName,
                "lastName" : lastName,
                "login" : login,
                "password" : password,
                    "mainRole": userRoles,
                "appSpecificRoles" : []
            });
        }

        onSaved();
    } catch (error) {
        handleSaveError(error);
    }
};

return (
    <>
    
        <InputText id="user-first-name" name="firstName" label={t("firstName", "First Name")} value={firstName} onChangeFunction={(e) => setFirstName(e.target.value)} />
        <InputText id="user-last-name" name="lastName" label={t("lastName", "Last Name")} value={lastName} onChangeFunction={(e) => setLastName(e.target.value)} />
        <InputText id="user-login" name="login" label={t("login", "Login")} value={login} onChangeFunction={(e) => setLogin(e.target.value)} />
        <InputPassword id="user-password" name="password" label={t("password", "Password")} type="password" value={password} onChangeFunction={(e) => setPassword(e.target.value)} />
        <div>
            <label htmlFor="user-roles" className="block text-sm font-medium text-gray-700 mt-4">{t("mainRole", "Main Role")}</label>
        <select id="user-roles" name="roles" value={userRoles} onChange={(e) => setUserRoles(e.target.value)} className="w-full p-2 border border-gray-300 rounded">
            {roles.map((role) => (
                <option key={role.name} value={role.name}>{role.name}</option>
            ))}
        </select>
        </div>
    
        <MultiSelect
        name={t("appSpecificRoles", "App Specific Roles")}
        options={roleName}
        selectedValues={userAppSpefRole}
        defaultValues={userAppSpefRole}
        disabled={false}
        onChangeFunction={setUserAppSpefRole}
        error={null}
        className={null}
        emptyLabel={t("none_selected", "None selected")}
        singleLabel={t("selected_one", "selected")}
        multipleLabel={t("selected_many", "selected")}
        />

        {saveError ? <p className="mt-3 text-red-600" role="alert">{saveError}</p> : null}



        <div className="flex justify-end mt-4">
            <Button label={t("cancel", "Cancel")} variant="secondary" className="mr-2" onClick={() => {onClose()}} />
            <Button 
            label={user ? t("save", "Save") : t("create", "Create")} 
            variant="primary" 
            onClick={handleSave} />
        </div>
    </>

    );
}

export default UserForm;