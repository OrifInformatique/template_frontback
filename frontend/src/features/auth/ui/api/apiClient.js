import axios from 'axios';
import useAuthStore from '../../authStore';

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

const hasAuthorizationHeader = (headers) =>
    headers?.has ? headers.has('Authorization') : !!headers?.Authorization;

api.interceptors.request.use(
    (config) => {
        const store = useAuthStore.getState ? useAuthStore.getState() : null;
        const token = store?.accessToken;
        if (token) config.headers.set('Authorization', `Bearer ${token}`);
        return config;
    },
    null,
    { synchronous: true }
);

let refreshPromise = null;

api.interceptors.response.use(
    (response) => {
        const newToken = response?.data?.accessToken || response?.data?.token;
        storeAccessToken(newToken);
        return response;
    },
    async (error) => {
        const status = error.response?.status;
        const originalRequest = error.config;

        // Refresh on 401 (expired token) or on 400 when the request was sent without Authorization.
        const missingAuthHeader = !hasAuthorizationHeader(originalRequest?.headers);
        const shouldRefresh =
            !!originalRequest && !originalRequest._retry && (status === 401 || (status === 400 && missingAuthHeader));

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
                originalRequest.headers.set('Authorization', `Bearer ${newToken}`);
                return api(originalRequest);
            }
        }

        return Promise.reject(error);
    }
);

export default api;
