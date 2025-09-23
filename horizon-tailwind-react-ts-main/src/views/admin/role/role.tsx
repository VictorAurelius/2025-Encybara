import DataTable from "../permission/components/data.table";
import { IRole } from './components/modal.role';
import { IPermission } from '../permission/components/modal.permission';
import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Space, Tag, message, notification } from "antd";
import { useState, useRef, useEffect } from 'react';
import dayjs from 'dayjs';
import ModalRole from "./components/modal.role";
import { ALL_PERMISSIONS } from "../permission/components/modules";
import { API_BASE_URL } from "service/api.config";
import Access from "../access";
import { App } from 'antd';

const RolePage = () => {
    const { message, notification } = App.useApp();
    const tableRef = useRef<ActionType>();

    const [openModal, setOpenModal] = useState<boolean>(false);
    const [dataInit, setDataInit] = useState<IRole | null>(null);
    const [dataSource, setDataSource] = useState<IRole[]>([]); // Khởi tạo state cho dataSource
    const [loading, setLoading] = useState<boolean>(true); // Khởi tạo state cho loading
    const [pageSize, setPageSize] = useState<number>(10); // Kích thước trang
    const [currentPage, setCurrentPage] = useState<number>(1); // Trang hiện tại
    const [total, setTotal] = useState<number>(0); // Thêm state cho tổng số lượng bản ghi


    //all backend permissions
    const [listPermissions, setListPermissions] = useState<{
        module: string;
        permissions: IPermission[]
    }[] | null>(null);

    //current role
    const [singleRole, setSingleRole] = useState<IRole | null>(null);

    useEffect(() => {
        const fetchPermissions = async () => {
            try {
                const response = await fetch(`${API_BASE_URL}/api/v1/permissions`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });
                if (!response.ok) {
                    throw new Error("Failed to fetch permissions");
                }
                const data = await response.json();
                console.log("data:", data.result);

                // Chuyển đổi dữ liệu
                const transformedData = data.result.reduce((acc: any, curr: any) => {
                    const moduleIndex = acc.findIndex((item: any) => item.module === curr.module);
                    if (moduleIndex > -1) {
                        acc[moduleIndex].permissions.push(curr);
                    } else {
                        acc.push({ module: curr.module, permissions: [curr] });
                    }
                    return acc;
                }, []);

                setListPermissions(transformedData);
            } catch (error) {
                console.error("Error fetching permissions:", error);
            }
        };

        fetchPermissions();
        reloadTable();
    }, []);

    const handleDeleteRole = async (id: number | undefined) => {
        if (!id) return;

        try {
            const res = await fetch(`${API_BASE_URL}/api/v1/roles/${id}`, {
                method: 'DELETE',
            });

            if (res.ok) {
                message.success('Delete role successfully');
                reloadTable();
            } else {
                const errorData = await res.json();
                notification.error({
                    message: 'An error occurred',
                    description: errorData.error || 'Cannot delete permission'
                });
            }
        } catch (error) {
            notification.error({
                message: 'Network error',
                description: 'Cannot connect to server'
            });
        }
        console.log("delete");
    }

    const reloadTable = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${API_BASE_URL}/api/v1/roles?page=${currentPage}&size=${pageSize}`);
            const data = await res.json();
            setDataSource(data.result); // Cập nhật dataSource
            setTotal(data.meta.total); // Cập nhật tổng số lượng bản ghi 

        } catch (error) {
            console.error("Error fetching data:", error);
        } finally {
            setLoading(false);
        }

    }

    const columns: ProColumns<IRole>[] = [
        {
            title: 'Id',
            dataIndex: 'id',
            width: 50,
            render: (text, record, index, action) => {
                return (
                    <span>
                        {record.id}
                    </span>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Name',
            dataIndex: 'name',
            sorter: true,
        },
        {
            title: 'Status',
            dataIndex: 'active',
            render(dom, entity, index, action, schema) {
                return <>
                    <Tag color={entity.active ? "lime" : "red"} >
                        {entity.active ? "ACTIVE" : "INACTIVE"}
                    </Tag>
                </>
            },
            hideInSearch: true,
        },
        {
            title: 'CreatedAt',
            dataIndex: 'createdAt',
            width: 200,
            sorter: true,
            render: (text, record, index, action) => {
                return (
                    <>{record.createdAt ? dayjs(record.createdAt).format('DD-MM-YYYY HH:mm:ss') : ""}</>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'UpdatedAt',
            dataIndex: 'updatedAt',
            width: 200,
            sorter: true,
            render: (text, record, index, action) => {
                return (
                    <>{record.updatedAt ? dayjs(record.updatedAt).format('DD-MM-YYYY HH:mm:ss') : ""}</>
                )
            },
            hideInSearch: true,
        },
        {

            title: 'Actions',
            hideInSearch: true,
            width: 50,
            render: (_value, entity, _index, _action) => (
                <Space>
                    <Access
                        permission={ALL_PERMISSIONS.SYSTEM_MANAGEMENT.PERMISSIONS.UPDATE}
                    >
                        <EditOutlined
                            style={{
                                fontSize: 20,
                                color: '#ffa500',
                            }}
                            type=""
                            onClick={() => {
                                setSingleRole(entity);
                                setOpenModal(true);
                            }} />
                    </Access>
                    <Access
                        permission={ALL_PERMISSIONS.SYSTEM_MANAGEMENT.PERMISSIONS.DELETE}
                    >
                        <Popconfirm
                            placement="leftTop"
                            title={"Confirm delete role"}
                            description={"Are you sure you want to delete this role ?"}
                            onConfirm={() => handleDeleteRole(entity.id)}
                            okText="Confirm"
                            cancelText="Cancel"
                        >
                            <span style={{ cursor: "pointer", margin: "0 10px" }}>
                                <DeleteOutlined
                                    style={{
                                        fontSize: 20,
                                        color: '#ff4d4f',
                                    }} />
                            </span>
                        </Popconfirm>
                    </Access>
                </Space>
            ),

        },
    ];
    return (
        <Access
            permission={{ module: "SYSTEM_MANAGEMENT" }}
        >
            <div>

                <DataTable<IRole>
                    actionRef={tableRef}
                    headerTitle="List Roles "
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    scroll={{ x: true }}
                    pagination={{
                        current: currentPage,
                        total: total,
                        pageSize: pageSize,
                        onChange: (page, size) => {
                            setCurrentPage(page); // Cập nhật trang hiện tại
                            setPageSize(size); // Cập nhật kích thước trang
                        },
                        showSizeChanger: true,
                    }
                    }
                    rowSelection={false}
                    toolBarRender={(_action, _rows): any => {
                        return (
                            <Access
                                permission={ALL_PERMISSIONS.SYSTEM_MANAGEMENT.PERMISSIONS.CREATE}
                            >
                                <Button
                                    icon={<PlusOutlined />}
                                    type="primary"
                                    onClick={() => setOpenModal(true)}
                                >
                                    Add
                                </Button>
                            </Access>
                        );
                    }}
                />
                <ModalRole
                    openModal={openModal}
                    setOpenModal={setOpenModal}
                    reloadTable={reloadTable}
                    listPermissions={listPermissions}
                    singleRole={singleRole}
                    setSingleRole={setSingleRole}
                />
            </div>
        </Access>
    )
}

export default () => (
    <App>
        <RolePage />
    </App>
);