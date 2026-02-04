import React, { useState } from 'react';

import { useTranslation } from 'react-i18next';
import { useLogin } from '../api/authService';
import useAuthStore from '../../authStore';
import {
    Button,
    Image,
    InputText,
    InputPassword,
} from '@orif-informatique/react-components-library';
import Link from '../../../../common/ui/link';
import LoginTestIndicator from '../../../../common/ui/tests/loginTestIndicator';

const Login = () => {
    const { t } = useTranslation("auth", "common");
    const [showLocalAccountLoginForm, setShowLocalAccountLoginForm] =
        useState(false);
    const { login } = useLogin();
    const accessToken = useAuthStore((state) => state.accessToken);

    return (
        <div className="flex flex-wrap place-content-center text-center w-full h-full">
            <div className="flex flex-col gap-4 w-full sm:w-[350px] h-fit p-8 border border-black sm:rounded-lg">
                <LoginTestIndicator/>
                <h1>{t("sign_in")}</h1>

                <div className="mx-auto">
                    <Link to="/oauth2/authorization/azure">
                        <Image
                            src="https://learn.microsoft.com/en-us/azure/active-directory/develop/media/howto-add-branding-in-apps/ms-symbollockup_signin_light.svg"
                            alt="Connexion avec Microsoft Azure"
                        />
                    </Link>
                </div>

                <hr className="border border-black" />

                {showLocalAccountLoginForm ? (
                    <>
                        <form onSubmit={login} className="flex flex-col gap-4">
                            <InputText
                                id="identifier"
                                name="identifier"
                                label={t("username")}
                                required={true}
                            />

                            <InputPassword
                                id="password"
                                name="password"
                                label={t("password")}
                                required={true}
                            />

                            <Button
                                variant="primary"
                                label={t("sign_in")}
                                className="w-full"
                            />
                        </form>

                        <Link to="/reset-password">
                            {t("forgot_password")}
                        </Link>
                    </>
                ) : (
                    <Button
                        variant="primary"
                        label={t("sign_in_with_local_account")}
                        onClick={() => setShowLocalAccountLoginForm(true)}
                    />
                )}
            </div>
        </div>
    );
};

export default Login;
