import React, { useEffect, useState, useMemo } from 'react';
import  { useTranslation } from 'react-i18next'
import useAuthStore from '../auth/authStore';

import { deleteUser, restoreUser, getUsers } from './api/api';
import { Button, PopUp, List } from '@orif-informatique/react-components-library';
import UserForm from './userForm';
import { apiErrorMessage } from '../auth/apiError';

function UserList() {

    const { t } = useTranslation('users');
    const [users, setUser] = useState([]);
    const token = useAuthStore(state => state.accessToken);
    const user = useAuthStore(state => state.user);
    const [showDeleted, setShowDeleted] = useState(false);
    const [formOpen, setFormOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [error, setError] = useState(null);
    const usersPermissions = user?.permissions || [];

        const fetchUsers = async () => {
                setError(null);
                try {
                    const response = await getUsers(showDeleted ? 'ALL' : 'ACTIVE');

                    setUser(response);
                } catch (error) {
                    console.error('Error fetching users:', error);
                    setUser([]);
                    setError(apiErrorMessage(error, t("fetch_error", "Failed to load users.")));
                }
            };

        // Runs a list action, then reloads the list; shows the error if it fails
        // (e.g. spring-auth refusing to delete the user's own account)
        const runAction = async (action) => {
            try {
                await action();
                await fetchUsers();
            } catch (error) {
                console.error('User action failed:', error);
                setError(apiErrorMessage(error, t("action_error", "The action failed.")));
            }
        };

        useEffect(() => {
            fetchUsers();
        }, [showDeleted]);

        const closeForm = () => {
            setFormOpen(false);
            setSelectedUser(null);
        };

        const handleUserSaved = async () => {
            closeForm();
            await fetchUsers();
        };

        const actions = useMemo(() => ({
            edit: { permission: "user:update", onClick: (user) => { setSelectedUser(user), setFormOpen(true)}},
            // Users are deleted in spring-auth and locally, softly or permanently
            delete: { permission: "user:delete", onClick: (user) => runAction(() => deleteUser(user.id)) },
            hardDelete: { permission: "user:delete", onClick: (user) => runAction(() => deleteUser(user.id, true)) },
            viewDeleted: { permission: "user:read"},
            restore: { permission: "user:update", onClick: (user) => runAction(() => restoreUser(user.id)) }
        }), [showDeleted]);


        const allowedAction = useMemo(() => 
            Object.entries(actions)
            .filter(([actionKey, action]) => usersPermissions.includes(action.permission))
            .reduce((acc, [actionKey, action]) => {
                acc[actionKey] = action;
                console.log(`Action "${actionKey}" is allowed for user with permissions:`, usersPermissions);
                return acc;
            }, {})
        , [user, actions]);

        return (
            <div>
                {formOpen ? (
                <PopUp
                    onClose={closeForm}
                    title={selectedUser ? t("edit_user", "Edit User") : t("create_user", "Create User")}
                    children={<UserForm user={selectedUser} onClose={closeForm} onSaved={handleUserSaved} />}
                />
                ) : null}
                {error && <p className="text-center text-red-500 py-4" role="alert">{error}</p>}
                {user?.permissions?.includes("user:write") && (
                    <Button label={t("create_user", "Create User")} variant="primary" className="mb-4" onClick={() => { setSelectedUser(null); setFormOpen(true); }} />
                )}

                <List
                    items={users}
                    actions={allowedAction}
                    actionsLabel={t("actions", "Actions")}
                    columns={['id', 'firstName', 'lastName', 'login', 'mainRole', 'appSpecificRoles']}
                    columnsLabels={{
                        id: '#',
                        firstName: t("firstName", "First Name"),
                        lastName: t("lastName", "Last Name"),
                        login: t("login", "login"),
                        mainRole: t("mainRole", "Main Role"),
                        appSpecificRoles: t("appSpecificRoles", "App Specific Roles")
                    }}
                    onToggleShowDeleted={setShowDeleted}
                    showDeletedLabel={t("show_deleted", "Show deleted items")}
                    showDeleted={showDeleted}
                    noItemsLabel={t("no_user_found", "No user found")}
                    confirmHardDeleteTitle={t("confirm_hard_delete", "Confirm Permanent Deletion")}
                    confirmHardDeleteText={t("confirm_hard_delete_text", "Are you sure you want to permanently delete this user ? This action cannot be undone.")}

                    isDeletedKey='deleted'
                />
            </div>
        );
    }

export default UserList;