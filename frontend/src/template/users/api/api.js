import api from "../../auth/apiClient";

// Every function throws the axios error when the request fails: callers show it
// (see apiErrorMessage in auth/apiError.js).

/**
 * Gets users filtered by soft-delete status.
 *
 * @param {'ACTIVE'|'DELETED'|'ALL'} status which subset to fetch (default 'ACTIVE')
 * @returns {Promise<Array>} the users
 */
export const getUsers = async (status = 'ACTIVE') => {
    const response = await api.get(`/users`, { params: { status } });
    return response.data;
};

/**
 * Gets the roles, main (owned by spring-auth) and/or local (app-specific).
 *
 * @param {'local'|'main'|'all'} scope which roles to fetch (default 'local')
 * @returns {Promise<Array>} the roles
 */
export const getRoles = async (scope = 'local') => {
    const response = await api.get(`/roles`, { params: { scope } });
    return response.data;
};

/**
 * Deletes a user, in spring-auth and locally (204 No Content).
 *
 * @param {number} userId the user id
 * @param {boolean} permanent true to remove the user, false for a soft (restorable) deletion
 */
export const deleteUser = async (userId, permanent = false) => {
    await api.delete(`/users/${userId}`, { params: { scope: 'GLOBAL', permanent } });
};

/**
 * Creates a user, in spring-auth and locally (201 Created).
 *
 * @returns {Promise<Object>} the created user
 */
export const createUser = async (data) => {
    const response = await api.post(`/users`, data);
    return response.data;
};

/**
 * Updates a user: profile and main role (in spring-auth), app-specific roles.
 *
 * @param {number} userId the user id
 * @returns {Promise<Object>} the updated user
 */
export const updateUser = async (userId, data) => {
    const response = await api.put(`/users/${userId}`, data);
    return response.data;
};

/**
 * Restores a soft-deleted user.
 *
 * @param {number} userId the user id
 * @returns {Promise<Object>} the restored user
 */
export const restoreUser = async (userId) => {
    const response = await api.post(`/users/${userId}/restore`);
    return response.data;
};
