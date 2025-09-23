import ApiService from './api.service';
import { API_ENDPOINTS } from './api.config';
import { IPermission } from 'views/admin/permission/components/modal.permission';
interface Admin {
    id: number;
    email: string;
    name: string;
    role: {
        permissions: IPermission[];
    }
}

interface LoginResponse {
    admin: Admin;
    access_token: string;  // Đổi tên field theo response
}

class AuthService {
    private api = ApiService();

    async login(credentials: { username: string; password: string }): Promise<LoginResponse> {
        try {
            const response = await this.api.post<LoginResponse>(
                API_ENDPOINTS.ADMIN.LOGIN,
                credentials
            );

            // Kiểm tra response có đúng format không
            if (!response || !response.access_token || !response.admin) {
                console.error('Invalid response structure:', response);
                throw new Error('Invalid response structure from server');
            }

            // Lưu token vào localStorage
            localStorage.setItem('admin_token', response.access_token);
            localStorage.setItem('admin', JSON.stringify(response.admin));

            return {
                admin: response.admin,
                access_token: response.access_token
            };
        } catch (error) {
            console.error('Login service error:', error);
            throw error;
        }
    }

    async logout(): Promise<void> {
        try {
            const token = localStorage.getItem('admin_token'); // Lấy token từ localStorage
            await this.api.post(API_ENDPOINTS.ADMIN.LOGOUT, {}, {
                headers: {
                    Authorization: `Bearer ${token}` // Truyền token vào header
                }
            });

            // Xóa token và user info khỏi localStorage
            localStorage.removeItem('admin_token');
            localStorage.removeItem('user');
        } catch (error) {
            console.error('Logout API error:', error);
            // Vẫn xóa local storage ngay cả khi API fail
            localStorage.removeItem('admin_token');
            localStorage.removeItem('user');
            throw error;
        }
    }

    getToken(): string | null {
        return localStorage.getItem('admin_token');
    }
}

export const authService = new AuthService();