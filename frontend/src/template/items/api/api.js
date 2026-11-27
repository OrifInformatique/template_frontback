import api from "../../auth/apiClient";

// Every function throws the axios error when the request fails: callers show it
// (see apiErrorMessage in auth/apiError.js).

/**
 * Gets items filtered by soft-delete status.
 *
 * @param {'ACTIVE'|'DELETED'|'ALL'} status which subset to fetch (default 'ACTIVE')
 * @returns {Promise<Array>} the items
 */
export const getItems = async (status = 'ACTIVE') => {
    const response = await api.get(`/items`, { params: { status } });
    return response.data;
};

/**
 * Updates the name and description of an item.
 *
 * @returns {Promise<Object>} the updated item
 */
export const modifyItem = async (id, data) => {
    const response = await api.put(`/items/${id}`, data);
    return response.data;
};

/**
 * Soft-deletes an item (204 No Content).
 */
export const deleteItem = async (id) => {
    await api.delete(`/items/${id}`);
};

/**
 * Restores a soft-deleted item.
 *
 * @returns {Promise<Object>} the restored item
 */
export const restoreItem = async (id) => {
    const response = await api.post(`/items/${id}/restore`);
    return response.data;
};

/**
 * Permanently deletes an item (204 No Content).
 */
export const hardDeleteItem = async (id) => {
    await api.delete(`/items/${id}`, { params: { permanent: true } });
};

/**
 * Creates an item (201 Created).
 *
 * @returns {Promise<Object>} the created item
 */
export const createItem = async (data) => {
    const response = await api.post(`/items`, data);
    return response.data;
};
