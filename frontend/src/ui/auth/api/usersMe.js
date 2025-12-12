import axios from 'axios';
import useAuthStore from '../../../../authStore';

/**
 * Call protected API to get current user information.
 * The access token is read from the zustand auth store.
 * @returns {Promise<any>} response data from the server
 */
export default async function usersMe() {
    const accessToken = useAuthStore.getState
        ? useAuthStore.getState().accessToken
        : null;

    console.log(accessToken);
    try {
        const resp = await axios.get('/users/me', {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        });
        return resp.data;
    } catch (err) {
        throw err;
    }
}
