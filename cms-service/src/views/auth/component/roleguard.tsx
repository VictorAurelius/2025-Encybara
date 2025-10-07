import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';

interface RoleGuardProps {
    children: React.ReactNode;
}

const RoleGuard: React.FC<RoleGuardProps> = ({ children }) => {
    const location = useLocation();
    const { token } = useAuth();

    console.log('RoleGuard - Current token:', token);
    console.log('RoleGuard - Current location:', location);

    // Kiểm tra đăng nhập thông qua token
    if (!token) {
        console.log('RoleGuard - No token, redirecting to login');
        // Lưu lại URL hiện tại để sau khi đăng nhập có thể quay lại
        return <Navigate to="/auth/sign-in" state={{ from: location }} replace />;
    }

    console.log('RoleGuard - Token valid, rendering children');
    // Nếu có token, cho phép truy cập
    return <>{children}</>;
};

