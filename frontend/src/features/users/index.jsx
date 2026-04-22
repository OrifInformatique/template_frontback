import React, { useEffect, useState, useMemo } from 'react';
import useAuthStore from '../auth/authStore';

import {deleteUserLocal, hardDeleteUserLocal, deleteUserDistant, hardDeleteUserDistant, restoreUser, getUsers, getUserWithDeleted} from './api/api';
import { Button, PopUp, List } from '@orif-informatique/react-components-library';
import UserForm from './userForm';

function UserList() {

    const [users, setUser] = useState([]);
    const token = useAuthStore(state => state.accessToken);
    const user = useAuthStore(state => state.user);
    const [showDeleted, setShowDeleted] = useState(false);
    const [formOpen, setFormOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const usersPermissions = user?.permissions || [];

        const fetchUsers = async () => {
                try {
                    const response = showDeleted 
                    ? await getUserWithDeleted()
                    : await getUsers();
                    
                    setUser(response);
                } catch (error) {
                    console.error('Error fetching users:', error);
                    setUser([]);
                }
            };

        useEffect(() => {
            fetchUsers();
        }, [showDeleted, users]);

        const actions = useMemo(() => ({
            edit: { permission: "user:update", onClick: (user) => { setSelectedUser(user); setFormOpen(true)}},
            delete: { permission: "user:delete", onClick: (user) => deleteUserLocal(user.id).then(() => deleteUserDistant(user.id).then(() => fetchUsers()).catch((err) => console.error("Delete failed:", err))) },
            hardDelete: { permission: "user:delete", onClick: (user) => hardDeleteUserLocal(user.id).then(() => fetchUsers()).catch((err) => console.error("Hard delete failed:", err)) },
            viewDeleted: { permission: "user:read"},
            restore: { permission: "user:update", onClick: (user) => restoreUser(user.id).then(() => fetchUsers()).catch((err) => console.error("Restore failed:", err)) }
        }), [showDeleted]);


        const allowedAction = useMemo(() => 
            Object.entries(actions)
            .filter(([actionKey, action]) => usersPermissions.includes(action.permission))
            .reduce((acc, [actionKey, action]) => {
                acc[actionKey] = action;
                console.log(`Action "${actionKey}" is allowed for user with permissions:`, usersPermissions);
                return acc;
            }, {})
        , [user]);

        return (
            <div>
                {formOpen ? (
                <PopUp
                    onClose={() => setFormOpen(false)}
                    title={selectedUser ? "Edit User" : "Create User"}
                    children={<UserForm user={selectedUser} onClose={() => setFormOpen(false)} />}
                />
                ) : null}
                {user?.permissions?.includes("user:write") && (
                    <Button label="Create User" variant="primary" className="mb-4" onClick={() => { setSelectedUser(null); setFormOpen(true); }} />
                )}
                <List
                    items={users}
                    actions={allowedAction}
                    columns={['id', 'firstName', 'lastName', 'login', 'mainRole']}
                    columnLabels={{
                        id: '#',
                        firstName: 'Prénom',
                        lastName: 'Nom de famille',
                        login: 'Identifiant',
                        mainRole: 'Rôle principal',
                    }}

                    onToggleShowDeleted={setShowDeleted}

                    showDeleted={showDeleted}

                    noItemsLabel="Aucun utilisateur trouvé."
                    confirmHardDeleteText="Êtes-vous sûr de vouloir supprimer définitivement cet utilisateur ? Cette action est irréversible."
                    confirmHardDeleteTitle="Confirmation de suppression définitive"

                    isDeletedKey='deleted'
                />
            </div>
        );
    }

export default UserList;