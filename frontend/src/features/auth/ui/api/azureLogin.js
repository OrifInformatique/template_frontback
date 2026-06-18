import useAuthStore from '../../authStore';
import api from './apiClient';

const BACKEND_API_URL = process.env.BACKEND_API_URL;
const AZURE_CALLBACK_PATH = '/azure';

const handleOAuth2Login = () => {
    const redirectUrl = `${window.location.origin}${AZURE_CALLBACK_PATH}`;
    const loginUrl = new URL('/auth/login/azure', BACKEND_API_URL);
    loginUrl.searchParams.set('redirectUrl', redirectUrl);
    window.location.assign(loginUrl.toString());
};

const getAccessToken = async () => {
    try {
        const response = await api.get('/auth/tokens', {
            baseURL: BACKEND_API_URL || undefined,
        });
        const accessToken = response?.data?.accessToken || response?.data?.token;
        if (accessToken) {
            useAuthStore.getState().setAccessToken(accessToken);
            return accessToken;
        }
        return null;
    } catch (error) {
        console.error('Error fetching access token:', error);
        return null;
    }
};

export { handleOAuth2Login, getAccessToken };