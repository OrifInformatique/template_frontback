import api from './httpClient';

/**
 * Call API to log out the current user.
 * @returns {Promise<void>}
 */
export default async function logOut() {
    await api.post('/auth/logout');
}