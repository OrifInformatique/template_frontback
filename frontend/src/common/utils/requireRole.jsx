import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuthStore from '../../features/auth/authStore';
import {SnackBar} from "@orif-informatique/react-components-library";


const RequireRole = ({ role, children }) => {
  const user = useAuthStore((s) => s.user);

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    // If a role does not match the specified role, redirect to the home page and put a snackbar message

    if (role && user.mainRole !== role) {
        return (
            <Navigate to="/" replace state={{ 
                snackbar: {
                    message: "You do not have permission to access this page.",
                    type: "error",
                    autoHideDuration: 3000,
                } 
            }}
        />);
    }

    return children || <Outlet />;
};

export default RequireRole;