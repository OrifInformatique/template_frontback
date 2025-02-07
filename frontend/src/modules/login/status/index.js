import React from 'react';

const Status = () => {
  const token = localStorage.getItem('token');
  const loginType = localStorage.getItem('loginType');

  if (!token) {
    return <div>User is logged out.</div>;
  }

  return (
    <div>
      User is logged in using {loginType === 'azure' ? 'Azure' : 'custom login'}.
    </div>
  );
};

export default Status;

