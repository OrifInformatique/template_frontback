import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(
    persist(
        (set) => ({
            accessToken: null,
            setAccessToken: (token) => set({ accessToken: token }),
            clearAccessToken: () => set({ accessToken: null }),
        }),
        { name: 'auth-storage', getStorage: () => sessionStorage }
    )
);

export default useAuthStore;
