import axios from 'axios';
import { API_BASE_URL } from './api.config';

const ApiService = () => {
    const api = axios.create({
        baseURL: API_BASE_URL,
        headers: {
            'Content-Type': 'application/json',
        },
    });

    // Request interceptor - Thêm token vào header
    api.interceptors.request.use(
        (config) => {
            const token = localStorage.getItem('admin_token');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
            return config;
        },
        (error) => Promise.reject(error)
    );

    // Response interceptor - Xử lý token hết hạn
    api.interceptors.response.use(
        (response) => response,
        (error) => {
            if (error.response?.status === 401) {
                // Token hết hạn hoặc không hợp lệ
                localStorage.removeItem('admin_token');
                localStorage.removeItem('user');
                window.location.href = '/auth/sign-in';
            }
            return Promise.reject(error);
        }
    );

    return {
        get: async <T = any>(url: string, config = {}): Promise<T> => {
            const response = await api.get<T>(url, config);
            return response.data;
        },
        post: async <T = any>(url: string, data = {}, config = {}): Promise<T> => {
            const response = await api.post<T>(url, data, config);
            return response.data;
        },
        put: async <T = any>(url: string, data = {}, config = {}): Promise<T> => {
            const response = await api.put<T>(url, data, config);
            return response.data;
        },
        delete: async <T = any>(url: string, config = {}): Promise<T> => {
            const response = await api.delete<T>(url, config);
            return response.data;
        },
    };
};

export default ApiService;