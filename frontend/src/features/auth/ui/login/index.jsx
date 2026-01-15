import React, { useEffect, useState } from 'react';
import axios from 'axios';

import { useLogin } from '../api/authService';
import useAuthStore from '../../authStore';
import {
    Button,
    Image,
    InputText,
    InputPassword,
} from '@orif-informatique/react-components-library';
import Link from '../../../../common/ui/link';
import LoginTestIndicator from './loginTestIndicator';

const Login = () => {
    const AUTH_API_URL = process.env.AUTH_API_URL;
    const [loginType, setLoginType] = useState(
        localStorage.getItem('loginType'),
    );
    const [showLocalAccountLoginForm, setShowLocalAccountLoginForm] =
        useState(false);
    const { login } = useLogin();
    const accessToken = useAuthStore((state) => state.accessToken);
    const { setAccessToken, clearAuth } = useAuthStore();

    useEffect(() => {
        const storedLoginType = localStorage.getItem('loginType');
        setLoginType(storedLoginType);
    }, []);

    useEffect(() => {
        // If we know the login type (user had a session) but lost the in-memory access token,
        // try to refresh using the HttpOnly refresh cookie.
        if (!loginType || accessToken) return;

        const refreshAccessToken = async () => {
            try {
                const response = await axios.post(
                    `${AUTH_API_URL}/auth/refresh`,
                    {},
                    { withCredentials: true },
                );
                if (response?.data?.accessToken) {
                    setAccessToken(response.data.accessToken);
                }
            } catch (error) {
                console.error(
                    '%c Token refresh failed',
                    'background: #D4000E; color: white; padding: 2px 5px; border-radius: 3px;',
                );
                console.error('Error details:', error.response?.data || error.message);
                // If refresh fails, clear stale login type so UI falls back to unauthenticated.
                localStorage.removeItem('loginType');
                setLoginType(null);
                clearAuth();
            }
        };

        refreshAccessToken();
    }, [AUTH_API_URL, accessToken, clearAuth, loginType, setAccessToken]);

    useEffect(() => {
        if (window.location.pathname !== '/oauth2/success') return;

        const params = new URLSearchParams(window.location.search);
        const tokenFromUrl = params.get('token');

        const handleToken = (jwt, type) => {
            localStorage.setItem('loginType', type);
            setLoginType(type);
            setAccessToken(jwt);
            window.history.replaceState({}, document.title, '/');
        };

        if (tokenFromUrl) {
            handleToken(tokenFromUrl, 'azure');
            return;
        }

        axios
            .get(`${AUTH_API_URL}/oauth2/success`, { withCredentials: true })
            .then((response) => handleToken(response.data.token, 'azure'))
            .catch((error) => {
                console.error(
                    '%c Azure Authentication Error',
                    'background: #D4000E; color: white; padding: 2px 5px; border-radius: 3px;',
                );
                console.error('Error details:', error.response?.data || error.message);
            });
    }, [AUTH_API_URL]);

    const handleOAuth2Login = () => {
        window.location.href = `${AUTH_API_URL}/oauth2/authorization/azure`;
    };

    const handleLogout = () => {
        localStorage.removeItem('loginType');
        clearAuth();
        setLoginType(null);
    };

    return (
        <div className="flex flex-wrap place-content-center text-center w-full h-full">
            <div className="flex flex-col gap-4 w-full sm:w-[350px] h-fit p-8 border border-black sm:rounded-lg">
                <LoginTestIndicator />
                <h1>Connexion</h1>

                {accessToken ? (
                    <div className="flex items-center justify-between gap-2 border border-black rounded-md p-2 text-sm">
                        <span>
                            Connecté via {loginType === 'azure' ? 'Azure' : 'local'}
                        </span>
                        <Button
                            variant="secondary"
                            label="Se déconnecter"
                            onClick={handleLogout}
                        />
                    </div>
                ) : null}

                <div className="mx-auto">
                    <button
                        onClick={handleOAuth2Login}
                        className="flex items-center gap-2 border border-black rounded-lg p-2 hover:bg-gray-200"
                    >
                        <Image
                            src="https://learn.microsoft.com/en-us/azure/active-directory/develop/media/howto-add-branding-in-apps/ms-symbollockup_signin_light.svg"
                            alt="Connexion avec Microsoft Azure"
                        />
                    </button>
                </div>

                <hr className="border border-black" />

                {!accessToken && showLocalAccountLoginForm ? (
                    <>
                        <form onSubmit={login} className="flex flex-col gap-4">
                            <InputText
                                id="identifier"
                                name="identifier"
                                label="Identifiant"
                                required={true}
                            />

                            <InputPassword
                                id="password"
                                name="password"
                                label="Mot de passe"
                                required={true}
                            />

                            <Button
                                variant="primary"
                                label="Se connecter"
                                className="w-full"
                            />
                        </form>

                        <Link to="/reset-password">
                            J'ai oublié mon mot de passe
                        </Link>
                    </>
                ) : !accessToken ? (
                    <Button
                        variant="primary"
                        label="Connexion avec un compte local"
                        onClick={() => setShowLocalAccountLoginForm(true)}
                    />
                ) : null}
            </div>
        </div>
    );
};

export default Login;
