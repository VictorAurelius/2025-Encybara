import React, { createContext, useContext, useState, useEffect } from 'react';

interface AuthContextType {
    token: string | null;
    admin: any | null;
    login: (token: string, adminData: any) => void;
    logout: () => void;
    setToken: (token: string | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(null);
    const [admin, setAdmin] = useState<any | null>(null);

    console.log("token vừa chạy", token);
    useEffect(() => {
        console.log("Token has been set:", token);
    }, [token]);


    const login = (newToken: string, adminData: any) => {
        setToken(newToken);
        setAdmin(adminData);
        localStorage.setItem('admin_token', newToken);
        localStorage.setItem('admin', JSON.stringify(adminData));
    };

    const logout = () => {
        setToken(null);
        setAdmin(null);
        localStorage.removeItem('admin_token');
        localStorage.removeItem('admin');
    };

    const handleSetToken = (newToken: string | null) => {
        if (newToken) {
            localStorage.setItem('admin_token', newToken);
        } else {
            localStorage.removeItem('admin_token');
        }
        setToken(newToken);
    };

    // Chỉ render khi đã load xong dữ liệu từ localStorage

    return (
        <AuthContext.Provider value={{ token, admin, login, logout, setToken: handleSetToken }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
