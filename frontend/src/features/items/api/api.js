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
        return includeDeleted ? items : items.filter((item) => !item.isDeleted);
    }
    catch(error)
    {
        console.error(`Error while fetching data: ${error.message}`);
        return [];
    }
};

