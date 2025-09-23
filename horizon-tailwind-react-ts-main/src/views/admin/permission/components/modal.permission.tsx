import { ModalForm, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { Col, Form, Row, message, notification } from "antd";
import { ALL_MODULES } from "../components/modules";
import { useEffect } from "react";
import { API_BASE_URL } from "service/api.config";

export interface IPermission {
    id?: number;
    name?: string;
    apiPath?: string;
    method?: string;
    module?: string;

    createdBy?: string;
    createdAt?: string;
    updatedAt?: string;

}
interface IProps {
    openModal: boolean;
    setOpenModal: (v: boolean) => void;
    dataInit?: IPermission | null;
    setDataInit: (v: any) => void;
    reloadTable: () => void;
}



const ModalPermission = (props: IProps) => {
    const { openModal, setOpenModal, reloadTable, dataInit, setDataInit } = props;
    const [form] = Form.useForm();

    useEffect(() => {
        if (dataInit) {
            form.setFieldsValue(dataInit);
        }
    }, [dataInit]);


    const submitPermission = async (valuesForm: any) => {


        if (dataInit?.id) {
            const { id, name, apiPath, method, module } = valuesForm;

            const permission = {
                id: dataInit?.id, // Đảm bảo lấy từ dataInit thay vì form
                ...valuesForm
            };

            const res = await fetch(`${API_BASE_URL}/api/v1/permissions`, { // Thêm id vào URL
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(permission), // Gửi dữ liệu
            });

            const responseData = await res.json();

            if (res.ok) {
                message.success("Cập nhật permission thành công");
                handleReset();
                reloadTable();
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: responseData.message || 'Lỗi không xác định',
                });
            }
        } else {
            //create
            const { name, apiPath, method, module } = valuesForm;
            const permission = {
                name,
                apiPath,
                method,
                module
            }
            const res = await fetch(`${API_BASE_URL}/api/v1/permissions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(permission), // Gửi dữ liệu
            });

            const responseData = await res.json();
            if (res.ok) {
                message.success("Thêm mới permission thành công");
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
        form.resetFields();
        setDataInit(null);
        setOpenModal(false);
    }

    return (
        <>
            <ModalForm
                title={<>{dataInit?.id ? "Update Permission" : "Create Permission"}</>}
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
                onFinish={submitPermission}

            >
                <Row gutter={16}>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="Permission"
                            name="name"
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Enter name"
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="API Path"
                            name="apiPath"
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Enter path"
                        />
                    </Col>

                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormSelect
                            name="method"
                            label="Method"
                            valueEnum={{
                                GET: 'GET',
                                POST: 'POST',
                                PUT: 'PUT',
                                PATCH: 'PATCH',
                                DELETE: 'DELETE',
                            }}
                            placeholder="Please select a method"
                            rules={[{ required: true, message: 'Please select a method!' }]}
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormSelect
                            name="module"
                            label="Module"
                            valueEnum={ALL_MODULES}
                            placeholder="Please select a module"
                            rules={[{ required: true, message: 'Please select a module!' }]}
                        />
                    </Col>

                </Row>
            </ModalForm>
        </>
    )
}

export default ModalPermission;
