import itemsData from "../mocks/items.json";
import api from "../../auth/ui/api/apiClient";

// Mutable copy of the mock data so mutations don't affect the original import
let items = [...itemsData];

/**
 * Gets all the items.
 *
 * @returns {Array}
 *
 */
export const getItems = async (includeDeleted = false) =>
{
    try
    {
        // Uncomment below to use the real backend.
        /*
        const response = await api.get(`/items`, { params: { includeDeleted } });
        return response.data;
        */
        return includeDeleted ? [...items] : items.filter((item) => !item.isDeleted);
    }
    catch(error)
    {
        console.error(`Error while fetching data: ${error.message}`);
        return [];
    }
};
export const modifyItem = async (id, data) =>
{
    try
    {
        // Uncomment below to use the real backend.
        /*
        const response = await api.put(`/items/${id}`, data);
        return response.data;
        */
        const index = items.findIndex((item) => item.id === id);
        if (index !== -1)
        {
            items[index] = { ...items[index], ...data };
            return items[index];
        }
        return null;
    }
    catch(error)
    {
        console.error(`Error while modifying item: ${error.message}`);
        return null;
    }
};
export const deleteItem = async (id) =>
{
    try
    {
        // Uncomment below to use the real backend.
        /*
        const response = await api.delete(`/items/${id}`);
        return response.data;
        */
        const index = items.findIndex((item) => item.id === id);
        if (index !== -1)
        {
            items[index].isDeleted = true;
            return items[index];
        }
        return null;
    }
    catch(error)
    {
        console.error(`Error while deleting item: ${error.message}`);
        return null;
    }
};
export const restoreItem = async (id) =>
{
    try
    {
        // Uncomment below to use the real backend.
        /*
        const response = await api.post(`/items/${id}/restore`);
        return response.data;
        */
        const index = items.findIndex((item) => item.id === id);
        if (index !== -1)
        {
            items[index].isDeleted = false;
            return items[index];
        }
        return null;
    }
    catch(error)
    {
        console.error(`Error while restoring item: ${error.message}`);
        return null;
    }
};
export const hardDeleteItem = async (id) =>
{
    try
    {
        // Uncomment below to use the real backend.
        /*
        const response = await api.delete(`/items/${id}/hard`);
        return response.data;
        */
        const index = items.findIndex((item) => item.id === id);
        if (index !== -1)
        {
            items.splice(index, 1);
            return { id };
        }
        return null;
    }
    catch(error)
    {
        console.error(`Error while hard deleting item: ${error.message}`);
        return null;
    }
};

export const createItem = async (data) =>
{
    try {
        // Uncomment below to use the real backend.
        /*
        const response = await api.post(`/items`, data);
        return response.data;
        */
        const newItem = {
            id: items.length ? Math.max(...items.map((item) => item.id)) + 1 : 1,
            name: data.name,
            description: data.description,
            author: data.author || "Unknown",
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            isDeleted: false,

        };
        items.push(newItem);
        return newItem;
    }
    catch(error)
    {
        console.error(`Error while creating item: ${error.message}`);
        return null;
    }
};