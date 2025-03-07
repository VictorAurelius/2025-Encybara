//import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Space, message, notification, Collapse, Tag, Row, Col, Empty } from "antd";
import { useState, useRef, useEffect } from 'react';
import dayjs from 'dayjs';
import Lottie from 'react-lottie';
import ModalPermission from "../permission/components/modal.permission";
import { colorMethod, groupByPermission } from "../permission/components/color.method";
import { API_BASE_URL } from "service/api.config";
//import { ALL_PERMISSIONS } from "../permission/components/modules";
interface IPermission {
    id?: number;
    name?: string;
    apiPath?: string;
    method?: string;
    module?: string;

    createdBy?: string;
    createdAt?: string;
    updatedAt?: string;

}
const PermissionPage = () => {
    const [openModal, setOpenModal] = useState<boolean>(false);
    const [dataInit, setDataInit] = useState<IPermission | null>(null);
    const [dataSource, setDataSource] = useState<IPermission[]>([]); // Khởi tạo state cho dataSource
    const [loading, setLoading] = useState<boolean>(true); // Khởi tạo state cho loading
    const [pageSize, setPageSize] = useState<number>(10); // Kích thước trang
    const [currentPage, setCurrentPage] = useState<number>(1); // Trang hiện tại
    const [total, setTotal] = useState<number>(0);

    const defaultOptions = {
        loop: true,
        autoplay: true,
        animationData: require('../../../assets/animations/permission.json'), // Thêm file JSON animation
        rendererSettings: {
            preserveAspectRatio: 'xMidYMid slice'
        }
    };
    const reloadTable = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${API_BASE_URL}/api/v1/permissions?page=1&size=1000`);
            const data = await res.json();
            console.log("data", data);
            setDataSource(data.result); // Cập nhật dataSource
            setTotal(data.meta.total); // Cập nhật tổng số lượng bản ghi 

        } catch (error) {
            console.error("Error fetching data:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        reloadTable(); // Gọi lại khi component mount hoặc khi currentPage/pageSize thay đổi
    }, [currentPage, pageSize]);

    const tableRef = useRef<ActionType>();

    const handleDeletePermission = async (id: number | undefined) => {
        if (!id) return;

        try {
            const res = await fetch(`${API_BASE_URL}/api/v1/permissions/${id}`, {
                method: 'DELETE',
            });

            if (res.ok) {
                message.success('Delete permission successfully');
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

    const columns: ProColumns<IPermission>[] = [
        {
            title: 'Id',
            dataIndex: 'id',
            width: 50,
            render: (text, record, index, action) => {
                return (
                    <a href="#" onClick={() => {
                        setDataInit(record);
                    }}>
                        {record.id}
                    </a>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Name',
            dataIndex: 'name',
            sorter: false,
        },
        {
            title: 'API',
            dataIndex: 'apiPath',
            sorter: false,
        },
        {
            title: 'Method',
            dataIndex: 'method',
            sorter: false,
            render(dom, entity, index, action, schema) {
                return (
                    <p style={{ paddingLeft: 10, fontWeight: 'bold', marginBottom: 0, color: colorMethod(entity?.method as string) }}>{entity?.method || ''}</p>
                )
            },
        },
        {
            title: 'Module',
            dataIndex: 'module',
            sorter: false,
        },
        {
            title: 'CreatedAt',
            dataIndex: 'createdAt',
            width: 200,
            sorter: false,
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
            sorter: false,
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
                    <EditOutlined
                        style={{
                            fontSize: 20,
                            color: '#ffa500',
                        }}

                        onClick={() => {
                            setOpenModal(true);
                            setDataInit(entity);
                        }} />
                    <Popconfirm
                        placement="leftTop"
                        title={"Confirm delete permission"}
                        description={"Are you sure you want to delete this permission ?"}
                        onConfirm={() => handleDeletePermission(entity.id)}
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
                </Space>
            ),

        },
    ];

    const renderPermissionItem = (permission: IPermission) => (
        <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '8px 0'
        }}>
            <div>
                <Tag color={colorMethod(permission.method || '')}>{permission.method}</Tag>
                <span style={{ marginLeft: 8 }}>{permission.apiPath}</span>
            </div>
            <Space>
                <EditOutlined
                    style={{ fontSize: 16, color: '#ffa500', cursor: 'pointer' }}
                    onClick={() => {
                        setOpenModal(true);
                        setDataInit(permission);
                    }}
                />
                <Popconfirm
                    title="Confirm delete permission"
                    description="Are you sure you want to delete this permission?"
                    onConfirm={() => handleDeletePermission(permission.id)}
                >
                    <DeleteOutlined style={{ fontSize: 16, color: '#ff4d4f', cursor: 'pointer' }} />
                </Popconfirm>
            </Space>
        </div>
    );

    return (
        <div style={{ position: 'relative' }}>
            <div style={{
                position: 'absolute',
                top: '90%',
                left: '50%',
                transform: 'translate(-50%, -40%)',
                zIndex: 0,
                opacity: 0.3
            }}>
                <Lottie
                    options={defaultOptions}
                    height={400}
                    width={400}
                />
            </div>

            <div style={{ position: 'relative', zIndex: 1 }}>
                <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', marginTop: 20 }}>
                    <h2>Permissions</h2>
                    <Button
                        icon={<PlusOutlined />}
                        type="primary"
                        onClick={() => setOpenModal(true)}
                    >
                        Add Permission
                    </Button>
                </div>

                <Row gutter={[16, 16]}>
                    {groupByPermission(dataSource).map(group => (
                        <Col span={12} key={group.module}>
                            <Collapse defaultActiveKey={[group.module]}>
                                <Collapse.Panel header={group.module} key={group.module}>
                                    {group.permissions.length > 0 ? (
                                        group.permissions.map(permission => (
                                            <div key={permission.id}>
                                                {renderPermissionItem(permission)}
                                            </div>
                                        ))
                                    ) : (
                                        <Empty
                                            image={
                                                <Lottie
                                                    options={defaultOptions}
                                                    height={200}
                                                    width={200}
                                                />
                                            }
                                            description="No permissions found"
                                        />
                                    )}
                                </Collapse.Panel>
                            </Collapse>
                        </Col>
                    ))}
                </Row>
            </div>

            <ModalPermission
                openModal={openModal}
                setOpenModal={setOpenModal}
                reloadTable={reloadTable}
                dataInit={dataInit}
                setDataInit={setDataInit}
            />
        </div>
    );
}

export default PermissionPage;