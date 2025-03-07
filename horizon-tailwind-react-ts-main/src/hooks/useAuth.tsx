import { useEffect, useState } from 'react';
import { authService } from '../service/auth.service';
import { IPermission } from 'views/admin/permission/components/modal.permission';

interface Admin {
    id: number;
    email: string;
    name: string;
    role: {
        permissions: IPermission[];
    }
}

interface LoginCredentials {
    username: string;
    password: string;
}

export const useAuth = () => {
    const [admin, setAdmin] = useState<Admin | null>(() => {
        const storedUser = localStorage.getItem("admin");
        return storedUser ? JSON.parse(storedUser) : null;
    });
    //sửa kh truyền được user sang Navbar
    const [token, setToken] = useState<string | null>(localStorage.getItem("admin_token"));
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const login = async (credentials: LoginCredentials) => {
        console.log('Logging in with credentials:', credentials);
        try {
            setLoading(true);
            setError(null);

            const response = await authService.login(credentials);
            console.log('Login response in useAuth:', response);
            localStorage.setItem("admin", JSON.stringify(response.admin)); // Lưu user
            localStorage.setItem("admin_token", response.access_token); // Lưu token
            setAdmin(response.admin);
            setToken(response.access_token);
            return response;
        } catch (err) {
            console.error('Login error in useAuth:', err);
            setError(err instanceof Error ? err.message : 'Đăng nhập thất bại');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const logout = async () => {
        console.log('Logging out...');
        try {
            await authService.logout();
            console.log('Logout successful');
            localStorage.clear();
            setToken(null);
            setAdmin(null);
        } catch (error) {
            console.error('Logout error:', error);
        }
    };

    return {
        admin,
        token,
        loading,
        error,
        login,
        logout,
        isAuthenticated: !!token
    };
};