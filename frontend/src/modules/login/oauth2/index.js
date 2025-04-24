import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const loginType = params.get('loginType');

    if (token) {
      localStorage.setItem('token', token);
      localStorage.setItem('loginType', loginType);
      navigate('/status');
    } else {
      // Handle error: token not found
      navigate('/login');
    }
  }, [navigate]);

  return <div>Processing...</div>;
};

export default OAuth2RedirectHandler;
