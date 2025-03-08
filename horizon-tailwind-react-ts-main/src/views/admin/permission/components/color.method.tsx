import { grey, green, blue, red, orange } from '@ant-design/colors';
import { IPermission } from './modal.permission';
import groupBy from 'lodash/groupBy';
import map from 'lodash/map';
export function colorMethod(method: "POST" | "PUT" | "GET" | "DELETE" | string) {
    switch (method) {
        case "POST":
            return green[6]
        case "PUT":
            return orange[6]
        case "GET":
            return blue[6]
        case "DELETE":
            return red[6]
        default:
            return grey[10];
    }
}
export const groupByPermission = (data: IPermission[]): { module: string; permissions: IPermission[] }[] => {
    // Nhóm permissions theo module
    const groupedData = groupBy(data, 'module');

    // Chuyển đổi object thành array và sắp xếp theo module
    return Object.entries(groupedData)
        .map(([module, permissions]) => ({
            module,
            permissions: permissions as IPermission[]
        }))
        .sort((a, b) => a.module.localeCompare(b.module));
};