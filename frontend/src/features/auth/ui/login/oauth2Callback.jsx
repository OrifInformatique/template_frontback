import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

import useAuthStore from '../../authStore';

const OAuth2Callback = () => {
    const navigate = useNavigate();
    const setAccessToken = useAuthStore((state) => state.setAccessToken);
    const setUser = useAuthStore((state) => state.setUser);
    const clearAuth = useAuthStore((state) => state.clearAuth);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const authCode = params.get('authCode') || params.get('code');
        const rawUserId = params.get('userId') || params.get('id');
        const tokenFromUrl = params.get('token');

        const finalizeLogin = (userData, token) => {
            if (token) {
                setAccessToken(token);
            }

            if (userData) {
                setUser(userData);
            }

            localStorage.setItem('loginType', 'azure');

            const redirectTarget = localStorage.getItem('postAzureLoginRedirect') || '/';
            localStorage.removeItem('postAzureLoginRedirect');
            navigate(redirectTarget === '/oauth2/login/success' ? '/' : redirectTarget, {
                replace: true,
            });
        };

        if (tokenFromUrl) {
            finalizeLogin(null, tokenFromUrl);
            return;
        }

        if (!authCode || !rawUserId) {
            setErrorMessage('Azure login did not return the expected authentication code.');
            return;
        }

        const userId = Number(rawUserId);
        if (Number.isNaN(userId)) {
            setErrorMessage('Azure login returned an invalid user identifier.');
            return;
        }

        axios
            .get('/auth/auth-code', {
                baseURL: process.env.BACKEND_API_URL || 'http://localhost:8081',
                withCredentials: true,
                params: {
                    authCode,
                    userId,
                },
            })
            .then((response) => {
                const userData = response?.data || null;
                const token = userData?.accessToken || userData?.token || null;
                finalizeLogin(userData, token);
            })
            .catch((error) => {
                clearAuth();
                localStorage.removeItem('loginType');
                setErrorMessage(
                    error.response?.data?.message ||
                    'Azure login failed during backend token exchange.',
                );
            });
    }, [clearAuth, navigate, setAccessToken, setUser]);

    return (
        <div className="flex flex-wrap place-content-center text-center w-full h-full">
            <div className="flex flex-col gap-4 w-full sm:w-[420px] h-fit p-8 border border-black sm:rounded-lg">
                <h1>Connexion Azure</h1>
                {errorMessage ? (
                    <>
                        <p>{errorMessage}</p>
                        <button
                            className="border border-black rounded-lg p-2 hover:bg-gray-200"
                            onClick={() => navigate('/login', { replace: true })}
                        >
                            Retour a la connexion
                        </button>
                    </>
                ) : (
                    <p>Connexion en cours...</p>
                )}
            </div>
        </div>
    );
};

export default OAuth2Callback;