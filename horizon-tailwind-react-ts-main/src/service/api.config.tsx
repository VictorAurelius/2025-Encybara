//export const API_BASE_URL = 'http://14.225.198.3:8080';
export const API_BASE_URL = 'http://localhost:8080';
export const API_ENDPOINTS = {
    ADMIN: {
        LOGIN: '/api/v1/admin/login',
        LOGOUT: '/api/v1/admin/logout',
        DASHBOARD: '/admin/dashboard',
        MANAGE_USERS: '/api/v1/users',
        SETTINGS: '/admin/settings',
        COURSE: '/api/v1/courses',
    },
    DOMAIN_ADMIN: {
        DASHBOARD: '/domain-admin/dashboard',
        REPORTS: '/domain-admin/reports',
        SETTINGS: '/domain-admin/settings',
    },
};