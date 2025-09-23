import { useEffect, useState } from 'react';
import { Result } from "antd";
import { useAuth } from '../../hooks/useAuth';
import { IPermission } from 'views/admin/permission/components/modal.permission';

interface IProps {
    hideChildren?: boolean;
    children: React.ReactNode;
    permission: {
        module?: string;      // Cho phép kiểm tra theo module
        method?: string;      // Method có thể optional
        apiPath?: string;     // API path có thể optional
        resource?: string;    // Thêm resource để kiểm tra theo nhóm API (vd: courses, questions)
    };
}

const Access = (props: IProps) => {
    const { permission, hideChildren = false } = props;
    const [allow, setAllow] = useState<boolean>(false);
    const { admin } = useAuth();

    useEffect(() => {
        if (admin?.role?.permissions?.length) {
            let hasPermission = false;

            // Kiểm tra theo module
            if (permission.module && !permission.apiPath && !permission.resource) {
                hasPermission = admin.role.permissions.some((item: IPermission) =>
                    item.module === permission.module
                );
            }

            // Kiểm tra theo resource (nhóm API)
            else if (permission.resource) {
                hasPermission = admin.role.permissions.some((item: IPermission) =>
                    item.apiPath.includes(permission.resource)
                );
            }

            // Kiểm tra theo API cụ thể
            else if (permission.apiPath) {
                // Kiểm tra quyền truy cập cụ thể
                const check = admin.role.permissions.find((item: IPermission) =>
                    (!permission.module || item.module === permission.module) &&
                    item.apiPath === permission.apiPath &&
                    (!permission.method || item.method === permission.method)
                );

                // Kiểm tra wildcard permission cho module
                const hasModuleWildcard = admin.role.permissions.find((item: IPermission) =>
                    (!permission.module || item.module === permission.module) &&
                    item.apiPath === '*' &&
                    item.method === '*'
                );

                hasPermission = !!check || !!hasModuleWildcard;
            }

            setAllow(hasPermission);
        }
    }, [admin?.role?.permissions, permission]);

    // Kiểm tra nếu ACL được tắt trong môi trường development
    const isAclDisabled = process.env.REACT_APP_ACL_ENABLE === 'false';

    if (isAclDisabled || allow) {
        return <>{props.children}</>;
    }

    // Nếu hideChildren = true, không hiển thị gì cả
    if (hideChildren) {
        return null;
    }

    // Hiển thị thông báo không có quyền truy cập
    return (
        <Result
            status="403"
            title="Truy cập bị từ chối"
            subTitle="Xin lỗi, bạn không có quyền truy cập chức năng này"
        />
    );
};

export default Access;