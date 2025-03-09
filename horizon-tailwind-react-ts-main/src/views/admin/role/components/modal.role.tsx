import { IPermission } from "views/admin/permission/components/modal.permission";
import { FooterToolbar, ModalForm, ProCard, ProFormSelect, ProFormSwitch, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { Col, Form, Row, message, notification } from "antd";
import { API_BASE_URL } from "service/api.config";
import ModuleApi from "./modal.api";

export interface IRole {
    id?: number;
    name: string;
    description: string;
    active: boolean;
    permissions: IPermission[] | string[];

    createdBy?: string;
    createdAt?: string;
    updatedAt?: string;
}
interface IProps {
    openModal: boolean;
    setOpenModal: (v: boolean) => void;
    reloadTable: () => void;
    listPermissions: {
        module: string;
        permissions: IPermission[]
    }[];
    singleRole: IRole | null;
    setSingleRole: (v: any) => void;
}
const ModalRole = (props: IProps) => {
    const { openModal, setOpenModal, reloadTable, listPermissions, singleRole, setSingleRole } = props;
    const [form] = Form.useForm();

    const submitRole = async (valuesForm: any) => {
        const { description, active, name, permissions } = valuesForm;
        const checkedPermissions = [];

        if (permissions) {
            for (const key in permissions) {
                if (key.match(/^[1-9][0-9]*$/) && permissions[key] === true) {
                    checkedPermissions.push({ id: parseInt(key) });
                }
            }
        }

        const role = {
            id: singleRole?.id,
            name,
            description,
            active,
            permissions: checkedPermissions
        };

        console.log("role:", role);

        const res = await fetch(`${API_BASE_URL}/api/v1/roles`, {
            method: singleRole?.id ? "PUT" : "POST",
            body: JSON.stringify(role),
            headers: {
                'Content-Type': 'application/json',
            },
        });

        const data = await res.json();
        if (res.ok) {
            message.success(singleRole?.id ? "Update role successfully" : "Create role successfully");
            handleReset();
            reloadTable();
        } else {
            notification.error({
                message: 'An error occurred',
                description: data.message
            });
        }
    };

    const handleReset = async () => {
        form.resetFields();
        setOpenModal(false);
        setSingleRole(null);
    }

    return (
        <>
            <ModalForm
                title={<>{singleRole?.id ? "Update Role" : "Create Role"}</>}
                open={openModal}
                modalProps={{
                    onCancel: () => { handleReset() },
                    afterClose: () => handleReset(),
                    destroyOnClose: true,
                    width: 900,
                    keyboard: false,
                    maskClosable: false,
                    okText: <>{singleRole?.id ? "Update" : "Create"}</>,
                    cancelText: "Cancel"

                }}
                scrollToFirstError={true}
                preserve={false}
                form={form}
                onFinish={submitRole}

            >
                <Row gutter={16}>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="Name Role"
                            name="name"
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Nhập name"
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormSwitch
                            label="Status"
                            name="active"
                            checkedChildren="ACTIVE"
                            unCheckedChildren="INACTIVE"
                            initialValue={true}
                            fieldProps={{
                                defaultChecked: true,
                            }}
                        />
                    </Col>

                    <Col span={24}>
                        <ProFormTextArea
                            label="Description"
                            name="description"
                            rules={[{ required: true, message: 'Please do not leave blank' }]}
                            placeholder="Enter description"
                            fieldProps={{
                                autoSize: { minRows: 2 }
                            }}
                        />
                    </Col>
                    <Col span={24}>
                        <ProCard
                            title="Permissions"
                            subTitle="The permissions allowed for this role"
                            headStyle={{ color: '#d81921' }}
                            style={{ marginBottom: 20 }}
                            headerBordered
                            size="small"
                            bordered
                        >
                            <ModuleApi
                                form={form}
                                listPermissions={listPermissions}
                                singleRole={singleRole}
                                openModal={openModal}
                            />

                        </ProCard>

                    </Col>
                </Row>
            </ModalForm>
        </>
    )
}
export default ModalRole;