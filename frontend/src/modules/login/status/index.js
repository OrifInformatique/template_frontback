import React from 'react';

const Status = () => {
  const token = localStorage.getItem('token');
  const loginType = localStorage.getItem('loginType');

  const decodeJWT = (token) => {
    try {
      const [, payload] = token.split('.');
      return JSON.parse(atob(payload));
    } catch (error) {
      console.error('Error decoding JWT:', error);
      return null;
    }
  };

  const formatDate = (timestamp) => {
    return new Date(timestamp * 1000).toLocaleString('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'medium'
    });
  };

  if (!token) {
    return <div>User is logged out.</div>;
  }

  const decodedToken = decodeJWT(token);
  const expirationDate = decodedToken?.exp ? formatDate(decodedToken.exp) : 'N/A';
  const issuedDate = decodedToken?.iat ? formatDate(decodedToken.iat) : 'N/A';
  const timeLeft = decodedToken?.exp ? Math.max(0, Math.floor((decodedToken.exp * 1000 - Date.now()) / 1000 / 60)) : 0;

  // Log token details to console
  console.log('JWT Token:', token);
  console.log('Decoded Token:', decodedToken);

  return (
    <div className="p-6 max-w-lg mx-auto bg-white rounded-xl shadow-md space-y-4">
      <h2 className="text-xl font-bold text-gray-900">
        Authentication Status
      </h2>
      
      <div className="space-y-2">
        <p className="text-blue-600 font-semibold">
          Connected via: {loginType === 'azure' ? 'Microsoft Azure' : 'Custom Login'}
        </p>

        {decodedToken && (
          <>
            <div className="space-y-1">
              <p><span className="font-semibold">First Name:</span> {decodedToken.firstName || 'N/A'}</p>
              <p><span className="font-semibold">Last Name:</span> {decodedToken.lastName || 'N/A'}</p>
              <p><span className="font-semibold">Email:</span> {decodedToken.sub || 'N/A'}</p>
            </div>

            <div className="space-y-1 mt-4">
              <p><span className="font-semibold">Token Validity:</span></p>
              <p>Issued at: {issuedDate}</p>
              <p>Expires at: {expirationDate}</p>
              <p className={`font-medium ${timeLeft < 5 ? 'text-red-600' : 'text-green-600'}`}>
                Time remaining: {timeLeft} minutes
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Status;

