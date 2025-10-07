import React, { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

interface ProtectedRouteProps {
    children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const location = useLocation();
    const { token } = useAuth();

    console.log("Token ở protectedroute", token);
    if (!token) {
        return <Navigate to="/auth/sign-in" state={{ from: location }} replace />;
    }

    return <>{children}</>;
};

export default ProtectedRoute;