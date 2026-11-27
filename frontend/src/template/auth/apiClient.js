import axios from 'axios';
import useAuthStore from './authStore';

const api = axios.create({
    withCredentials: true,
    baseURL: process.env.BACKEND_API_URL || 'http://localhost:8081',
});

const refreshClient = axios.create({
    withCredentials: true,
    baseURL: process.env.BACKEND_API_URL || 'http://localhost:8081',
});

const storeAccessToken = (token) => {
    if (!token) return;
    const store = useAuthStore.getState ? useAuthStore.getState() : null;
    if (store?.setAccessToken) store.setAccessToken(token);
};

api.interceptors.request.use((config) => {
    const store = useAuthStore.getState ? useAuthStore.getState() : null;
    const token = store?.accessToken;
    if (token) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

let refreshPromise = null;

// Requests whose 401 must not trigger a token refresh
const NO_REFRESH_URLS = ['/auth/login', '/auth/refresh', '/oauth2/token'];

api.interceptors.response.use(
    (response) => {
        const newToken = response?.data?.accessToken || response?.data?.token;
        storeAccessToken(newToken);
        return response;
    },
    async (error) => {
        const status = error.response?.status;
        const originalRequest = error.config;

        // Refresh on 401 (missing or expired access token), except for the requests
        // that obtain tokens themselves: their 401 means wrong credentials or no
        // Azure login to complete, which a refresh can't fix.
        const tokenRequest = NO_REFRESH_URLS.some((url) => originalRequest?.url?.startsWith(url));
        const shouldRefresh =
            !!originalRequest && !originalRequest._retry && status === 401 && !tokenRequest;

        if (shouldRefresh) {
            originalRequest._retry = true;

            refreshPromise =
                refreshPromise ||
                refreshClient
                    .post('/auth/refresh')
                    .then((res) => {
                        refreshPromise = null;
                        const newToken = res.data?.accessToken || res.data?.token;
                        storeAccessToken(newToken);
                        return newToken;
                    })
                    .catch((refreshError) => {
                        refreshPromise = null;
                        const store = useAuthStore.getState ? useAuthStore.getState() : null;
                        if (store?.clearAuth) store.clearAuth();
                        throw refreshError;
                    });

            const newToken = await refreshPromise;
            if (newToken) {
                originalRequest.headers = originalRequest.headers || {};
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
                return api(originalRequest);
            }
        }

        return Promise.reject(error);
    }
);

export default api;
