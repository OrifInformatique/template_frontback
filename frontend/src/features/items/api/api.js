import items from "../mocks/items.json";
import api from "../../auth/ui/api/apiClient";

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
        const response = await api.get(`/items?includeDeleted=${includeDeleted}`);

        if (!response.ok)
        {
            const error = await response.text();
            console.error(`${response.status} ${response.statusText} : ${error}`);
            return [];
        }

        return await response.json();
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

        if (!response.ok)
        {
            const error = await response.text();
            console.error(`${response.status} ${response.statusText} : ${error}`);
            return null;
        }

        return await response.json();
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

        if (!response.ok)
        {
            const error = await response.text();
            console.error(`${response.status} ${response.statusText} : ${error}`);
            return null;
        }

        return await response.json();
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

        if (!response.ok)
        {
            const error = await response.text();
            console.error(`${response.status} ${response.statusText} : ${error}`);
            return null;
        }

        return await response.json();
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

        if (!response.ok)
        {
            const error = await response.text();
            console.error(`${response.status} ${response.statusText} : ${error}`);
            return null;
        }

        return await response.json();
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