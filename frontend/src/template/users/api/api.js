import api from "../../auth/apiClient";

/**
 * Gets users filtered by soft-delete status.
 *
 * @param {'ACTIVE'|'DELETED'|'ALL'} status which subset to fetch (default 'ACTIVE')
 */
export const getUsers = async (status = 'ACTIVE') => {
    try {
        const response = await api.get(`/users`, { params: { status } });
        return response.data;
    }
    catch(error) {
        console.error(`Error while fetching users: ${error.message}`);
        return [];
    }
};

/**
 * Gets the roles, main (owned by spring-auth) and/or local (app-specific).
 *
 * @param {'local'|'main'|'all'} scope which roles to fetch (default 'local')
 */
export const getRoles = async (scope = 'local') => {
    try {
        const response = await api.get(`/roles`, { params: { scope } });
        return response.data;
    }
    catch(error) {
        console.error(`Error while fetching roles: ${error.message}`);
        return [];
    };
}

/**
 * Deletes a user, in spring-auth and locally (204 No Content).
 *
 * @param {number} userId the user id
 * @param {boolean} permanent true to remove the user, false for a soft (restorable) deletion
 */
export const deleteUser = async (userId, permanent = false) => {
    try {
        await api.delete(`/users/${userId}`, { params: { scope: 'GLOBAL', permanent } });
        return true;
    }
    catch(error) {
        console.error(`Error while deleting user: ${error.message}`);
        return false;
    }
};

/**
 * Creates a user, in spring-auth and locally.
 *
 * @returns the created user
 */
export const createUser = async (data) => {
    try {
        const response = await api.post(`/users`, data);
        return response.data;
    }
    catch(error) {
        console.error(`Error while creating user: ${error.message}`);
        throw error;
    }
};

/**
 * Updates a user: profile and main role (in spring-auth), app-specific roles.
 *
 * @param {number} userId the user id
 * @returns the updated user
 */
export const updateUser = async (userId, data) => {
    try {
        const response = await api.put(`/users/${userId}`, data);
        return response.data;
    }
    catch(error) {
        console.error(`Error while updating user: ${error.message}`);
        throw error;
    }
};

/**
 * Restores a soft-deleted user.
 *
 * @param {number} userId the user id
 * @returns the restored user
 */
export const restoreUser = async (userId) => {
    try {
        const response = await api.post(`/users/${userId}/restore`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while restoring user: ${error.message}`);
        return null;
    }
};
