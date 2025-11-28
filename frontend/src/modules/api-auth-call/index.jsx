import React, { useState } from 'react';

import { Button,
         PopUp
       } from "@orif-informatique/react-components-library";
import useAuthStore from '../../../authStore';
import axios from 'axios';

const ApiAuthCall = () => {
    const accessToken = useAuthStore((state) => state.accessToken);
    const [open, setOpen] = useState(false);
    const [apiResult, setApiResult] = useState(null);

    const callApi = async () => {
        try {
            const userResponse = await axios.get('/tests/me', {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                },
            });
            console.log(userResponse);
            setApiResult(userResponse.data);
            
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
                        <p>{accessToken}</p>
                        <p>{apiResult.firstName}</p>
                        <p>{apiResult.lastName}</p>
                        <p>{apiResult.login}</p>
                        <p>{apiResult.role}</p>
                        <p>{apiResult.token}</p>
                </PopUp>
            )}
        </div>
    );
};

export default ApiAuthCall;
