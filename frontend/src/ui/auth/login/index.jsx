import React, { useState } from 'react';

import Button from "../../buttons/default/Button";
import Image from '../../image/Image';
import InputText from "../../inputs/text/InputText";
import InputPassword from "../../inputs/password/InputPassword"
import Link from '../../link';

const Login = () =>
{
    const [showLocalAccountLoginForm, setShowLocalAccountLoginForm] = useState(false);

    const handleLoginFormSubmit = (e) =>
    {
        e.preventDefault();
        console.log(handleLoginFormSubmit);
    }

    return (
        <div className="flex flex-wrap place-content-center text-center w-full h-full">
            <div className="flex flex-col gap-4 w-full sm:w-[350px] h-fit p-8 border border-black sm:rounded-lg">
                <h1>Connexion</h1>

                <div className="mx-auto">
                    <Link to="/azure">
                        <Image
                            src="https://learn.microsoft.com/en-us/azure/active-directory/develop/media/howto-add-branding-in-apps/ms-symbollockup_signin_light.svg"
                            alt="Connexion avec Microsoft Azure"
                        />
                    </Link>
                </div>

                <hr className="border border-black" />

                {showLocalAccountLoginForm ? (
                    <>
                        <form
                            onSubmit={handleLoginFormSubmit}
                            className="flex flex-col gap-4"
                        >
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
                ) : (
                    <Button
                        variant="primary"
                        label="Connexion avec un compte local"
                        onClick={() => setShowLocalAccountLoginForm(true)}
                    />
                )}
            </div>
        </div>
    )
}

export default Login;