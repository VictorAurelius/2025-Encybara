export const ALL_PERMISSIONS = {
    SYSTEM_MANAGEMENT: {
        PERMISSIONS: {
            GET_PAGINATE: { method: "GET", apiPath: '/api/v1/permissions', module: "SYSTEM_MANAGEMENT" },
            CREATE: { method: "POST", apiPath: '/api/v1/permissions', module: "SYSTEM_MANAGEMENT" },
            UPDATE: { method: "PUT", apiPath: '/api/v1/permissions', module: "SYSTEM_MANAGEMENT" },
            DELETE: { method: "DELETE", apiPath: '/api/v1/permissions/{id}', module: "SYSTEM_MANAGEMENT" },
        },
        ROLES: {
            GET_PAGINATE: { method: "GET", apiPath: '/api/v1/roles', module: "SYSTEM_MANAGEMENT" },
            CREATE: { method: "POST", apiPath: '/api/v1/roles', module: "SYSTEM_MANAGEMENT" },
            UPDATE: { method: "PUT", apiPath: '/api/v1/roles', module: "SYSTEM_MANAGEMENT" },
            DELETE: { method: "DELETE", apiPath: '/api/v1/roles/{id}', module: "SYSTEM_MANAGEMENT" },
        },
        ADMINS: {
            GET_PAGINATE: { method: "GET", apiPath: '/api/v1/admins', module: "SYSTEM_MANAGEMENT" },
            CREATE: { method: "POST", apiPath: '/api/v1/admins', module: "SYSTEM_MANAGEMENT" },
            UPDATE: { method: "PUT", apiPath: '/api/v1/admins', module: "SYSTEM_MANAGEMENT" },
            DELETE: { method: "DELETE", apiPath: '/api/v1/admins/{id}', module: "SYSTEM_MANAGEMENT" },
        },
    },
    CONTENT_MANAGEMENT: {
        COURSES: {
            GET_PAGINATE: { method: "GET", apiPath: '/api/v1/courses', module: "CONTENT_MANAGEMENT" },
            GET_BY_ID: { method: "GET", apiPath: '/api/v1/courses/{id}', module: "CONTENT_MANAGEMENT" },
            CREATE: { method: "POST", apiPath: '/api/v1/courses', module: "CONTENT_MANAGEMENT" },
            UPDATE: { method: "PUT", apiPath: '/api/v1/courses', module: "CONTENT_MANAGEMENT" },
            DELETE: { method: "DELETE", apiPath: '/api/v1/courses/{id}', module: "CONTENT_MANAGEMENT" },
            ADD_LESSON: { method: "POST", apiPath: '/api/v1/courses/{id}/lessons', module: "CONTENT_MANAGEMENT" },
        },
        LESSONS: {
            GET_PAGINATE: { method: "GET", apiPath: '/api/v1/lessons', module: "CONTENT_MANAGEMENT" },
            GET_BY_ID: { method: "GET", apiPath: '/api/v1/lessons/{id}', module: "CONTENT_MANAGEMENT" },
            CREATE: { method: "POST", apiPath: '/api/v1/lessons', module: "CONTENT_MANAGEMENT" },
            UPDATE: { method: "PUT", apiPath: '/api/v1/lessons', module: "CONTENT_MANAGEMENT" },
            DELETE: { method: "DELETE", apiPath: '/api/v1/lessons/{id}', module: "CONTENT_MANAGEMENT" },
            ADD_QUESTION: { method: "POST", apiPath: '/api/v1/lessons/{id}/questions', module: "CONTENT_MANAGEMENT" },
        },
        QUESTIONS: {
            GET_PAGINATE: { method: "GET", apiPath: '/api/v1/questions', module: "CONTENT_MANAGEMENT" },
            GET_BY_ID: { method: "GET", apiPath: '/api/v1/questions/{id}', module: "CONTENT_MANAGEMENT" },
            CREATE: { method: "POST", apiPath: '/api/v1/questions', module: "CONTENT_MANAGEMENT" },
            UPDATE: { method: "PUT", apiPath: '/api/v1/questions', module: "CONTENT_MANAGEMENT" },
            DELETE: { method: "DELETE", apiPath: '/api/v1/questions/{id}', module: "CONTENT_MANAGEMENT" },
        },
    },
}
export const ALL_MODULES = {
    SYSTEM_MANAGEMENT: 'SYSTEM_MANAGEMENT',
    CONTENT_MANAGEMENT: 'CONTENT_MANAGEMENT'
}
export const DEFAULT_ROLES = {

    TECHNICAL_ADMIN: {
        name: "Technical Admin",
        permissions: [
            "SYSTEM_MANAGEMENT.*",  // Toàn quyền quản lý hệ thống
            "CONTENT_MANAGEMENT.*",    // Toàn quyền quản lý người dùng
        ]
    },

    // Admin nội dung cấp cao
    SENIOR_CONTENT_ADMIN: {
        name: "Senior Content Admin",
        permissions: [
            "CONTENT_MANAGEMENT.*"
        ]
    },
}
