import items from "../mocks/items.json";
import api from "../../auth/ui/api/httpClient";

/**
 * Gets all the items.
 *
 * @returns {Array}
 *
 */
export const getItems = async () =>
{
    try
    {
        // Uncomment below to use the real backend.
        /*
        const response = await api.get(`/items`);

        if (!response.ok)
        {
            const error = await response.text();
            console.error(`${response.status} ${response.statusText} : ${error}`);
            return [];
        }

        return await response.json();
        */
        return items;
    }
    catch(error)
    {
        console.error(`Error while fetching data: ${error.message}`);
        return [];
    }
};