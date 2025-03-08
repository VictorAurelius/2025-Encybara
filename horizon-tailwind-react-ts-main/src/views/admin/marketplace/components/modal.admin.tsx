import { ModalForm, ProForm, ProFormDigit, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { Col, Form, Row, message, notification } from "antd";
import { useState, useEffect } from "react";
import { DebounceSelect } from "../components/debouce.select";
import { API_BASE_URL } from "service/api.config";

export interface IAdmin {
    id?: number;
    name: string;
    email: string;
    password?: string;
    field?: string;
    role?: {
        id: number;
        name: string;
    }

    createdBy?: string;
    createdAt?: string;
    updatedAt?: string;
}

interface IRoleOption {
    label: string;
    value: string;
}

interface IProps {
    openModal: boolean;
    setOpenModal: (v: boolean) => void;
    dataInit?: IAdmin | null;
    setDataInit: (v: any) => void;
    reloadTable: () => void;
}


const ModalAdmin = (props: IProps) => {
    const { openModal, setOpenModal, reloadTable, dataInit, setDataInit } = props;
    const [roles, setRoles] = useState<IRoleOption[]>([]);
    const [form] = Form.useForm();

    useEffect(() => {
        if (dataInit?.id) {
            form.setFieldsValue({
                ...dataInit,
                role: { label: dataInit.role?.name, value: dataInit.role?.id }
            });
        }
    }, [dataInit, form]);
    const submitAdmin = async (valuesForm: any) => {
        const { name, email, password, field, role } = valuesForm;
        if (dataInit?.id) {
            //update
            const admin = {
                id: dataInit.id,
                name,
                email,
                password,
                field,
                role: { id: role.value, name: "" }
            }

            const res = await fetch(`${API_BASE_URL}/api/v1/admins`, { // Thêm id vào URL
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(admin), // Gửi dữ liệu
            });

            const responseData = await res.json();

            if (res.ok) {
                message.success("Update admin successfully");
                handleReset();
                reloadTable();
            } else {
                notification.error({
                    message: 'An error occurred',
                    description: responseData.message || 'Unknown error',
                });
            }
        } else {
            //create
            const admin = {
                name,
                email,
                password,
                field,
                role: { id: role.value, name: "" }
            }
            const res = await fetch(`${API_BASE_URL}/api/v1/admins`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(admin), // Gửi dữ liệu
            });

            const responseData = await res.json();
            if (res.ok) {
                message.success("Add new admin successfully");
                handleReset();
                reloadTable();
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: responseData.message || 'Lỗi không xác định',
                });
            }
        }
    }

    const handleReset = async () => {
        console.log("form1:", form.getFieldsValue());
        form.resetFields();
        console.log("form2:", form.getFieldsValue());
        setDataInit(null);
        setRoles([]);
        setOpenModal(false);
    }


    async function fetchRoleList(name: string): Promise<IRoleOption[]> {
        const res = await fetch(`${API_BASE_URL}/api/v1/roles`);
        const data = await res.json();
        console.log("data", data);
        if (data) {

            const list = data.result;
            const temp = list.map((item: any) => {
                return {
                    label: item.name as string,
                    value: item.id as string
                }
            })
            return temp;
        } else return [];
    }

    return (
        <>
            <ModalForm
                title={<>{dataInit?.id ? "Update Admin" : "Create Admin"}</>}
                open={openModal}
                modalProps={{
                    onCancel: () => { handleReset() },
                    destroyOnClose: true,
                    width: 900,
                    keyboard: false,
                    maskClosable: false,
                    okText: <>{dataInit?.id ? "Update" : "Create"}</>,
                    cancelText: "Cancel"
                }}
                scrollToFirstError={true}
                preserve={false}
                form={form}
                onFinish={submitAdmin}


            >
                <Row gutter={16}>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="Email"
                            name="email"
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                                { type: 'email', message: 'Please enter a valid email' }
                            ]}
                            placeholder="Enter email"
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText.Password
                            disabled={dataInit?.id ? true : false}
                            label="Password"
                            name="password"
                            rules={[{ required: dataInit?.id ? false : true, message: 'Please do not leave blank' }]}
                            placeholder="Enter password"
                        />
                    </Col>
                    <Col lg={6} md={6} sm={24} xs={24}>
                        <ProFormText
                            label="Display name"
                            name="name"
                            rules={[{ required: true, message: 'Please do not leave blank' }]}
                            placeholder="Enter display name"
                        />
                    </Col>
                    <Col lg={6} md={6} sm={24} xs={24}>
                        <ProFormSelect
                            label="Field"
                            name="field"
                            valueEnum={{
                                IT: 'IT',
                                CONSTRUCTION: 'CONSTRUCTION',
                                ECONOMIC: 'ECONOMIC',
                                ELECTRICITY: 'ELECTRICITY',
                            }}
                            rules={[{ required: true, message: 'Please do not leave blank' }]}
                            placeholder="Enter field"
                        />
                    </Col>
                    <Col lg={6} md={6} sm={24} xs={24}>
                        <ProForm.Item
                            name="role"
                            label="Role"
                            rules={[{ required: true, message: 'Please select a role!' }]}

                        >
                            <DebounceSelect
                                allowClear
                                showSearch
                                defaultValue={roles}
                                value={roles}
                                placeholder="Select role"
                                fetchOptions={fetchRoleList}
                                onChange={(newValue: any) => {
                                    setRoles(newValue as IRoleOption[]);
                                }}
                                style={{ width: '100%' }}
                            />
                        </ProForm.Item>

                    </Col>

                </Row>
            </ModalForm >
        </>
    )
}

export default ModalAdmin;
