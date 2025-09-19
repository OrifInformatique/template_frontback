import React, { useState } from 'react';
import Button from '../../ui/buttons/default/Button';
import PopUp from '../../ui/pop-up/PopUp';
import useAuthStore from '../../../authStore';
import axios from 'axios';

const AuthApiCall = () => {
    const accessToken = useAuthStore((state) => state.accessToken);
    const [open, setOpen] = useState(false);
    const [apiResult, stApiResult] = useState(null);

    const callApi = async () => {
        try {
            const userResponse = await axios.get('/users/me', {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                },
            });
        } catch (error) {
            console.log(error);
        }
        setOpen(true);
    };

    return (
        <div>
            <Button
                variant="primary"
                label="Test API Call"
                onClick={() => callApi()}
            />
            {open && (
                <PopUp title="Test" onClose={() => setOpen(false)}>
                    <p>{userResponse}</p>
                </PopUp>
            )}
        </div>
    );
};

export default AuthApiCall;
