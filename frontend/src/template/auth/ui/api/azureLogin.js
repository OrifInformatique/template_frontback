import useAuthStore from '../../authStore';
import api from '../../apiClient';

const BACKEND_API_URL = process.env.BACKEND_API_URL;
const AZURE_CALLBACK_PATH = '/azure';

const handleOAuth2Login = () => {
    const redirectUrl = `${window.location.origin}${AZURE_CALLBACK_PATH}`;
    const loginUrl = new URL('/oauth2/login/azure', BACKEND_API_URL);
    loginUrl.searchParams.set('redirectUrl', redirectUrl);
    window.location.assign(loginUrl.toString());
};

const getAccessToken = async () => {
    try {
        // Result of the Azure login kept in the session by the backend: single
        // use, the backend removes it once returned
        const response = await api.post('/oauth2/token', null, {
            baseURL: BACKEND_API_URL || undefined,
        });
        const data = response?.data || {};
        const accessToken = data.accessToken || data.token;
        const user = data.user || (data.id ? data : null);

        if (accessToken) {
            const store = useAuthStore.getState();
            store.setAccessToken(accessToken);
            if (user) store.setUser(user);
            return accessToken;
        }
        return null;
    } catch (error) {
        console.error('Error fetching access token:', error);
        return null;
    }
};

export { handleOAuth2Login, getAccessToken };