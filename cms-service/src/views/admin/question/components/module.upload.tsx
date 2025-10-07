import { FooterToolbar, ModalForm, ProCard, ProFormSelect, ProFormSwitch, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { Col, Form, Row, message, notification, Input, Button, Switch, Upload, Typography, Table, Empty } from "antd";
import { API_BASE_URL } from "service/api.config";
import { useEffect, useState } from "react";
import { CheckCircleOutlined, VideoCameraOutlined } from "@ant-design/icons";
import { IQuestion } from "./module.question";
import { App } from 'antd';

export interface IUpload {
    id?: number;
    materType: string;
    materLink: string;
    uploadedAt: string;
}
interface IProps {
    openModalUpload: boolean;
    setOpenModalUpload: (v: boolean) => void;
    reloadTable: () => void;
    uploadData: IUpload[] | null;
    quesID: number;
}

const { Title } = Typography;

const ModalUpload = (props: IProps) => {
    const { openModalUpload, setOpenModalUpload, reloadTable, uploadData, quesID } = props;
    const [form] = Form.useForm();
    const [upload, setUpload] = useState<IUpload | null>(null);
    const { message, notification } = App.useApp();
    useEffect(() => {
        if (uploadData && uploadData.length > 0) {
            // Thiết lập giá trị cho form
            const initialValues = uploadData.map((materialData, index) => ({
                materType: materialData.materType,
                materLink: materialData.materLink,
                uploadedAt: materialData.uploadedAt,
            }));

            form.setFieldsValue({
                materials: initialValues,
            });
        } else {
            // Reset form khi không có dữ liệu
            form.resetFields();
        }
    }, [uploadData, form]);
    const submitUpload = async (valuesForm: any) => {
        const { videoFile } = valuesForm; // Lấy file từ form
        const materLink = videoFile.file.name;
        const materType = videoFile.file.type.split('/').pop(); // Lấy materType từ uploadData
        const formData = {
            materLink: materLink,
            materType: materType,
            questionId: quesID,
        }
        try {
            const res = await fetch(`${API_BASE_URL}/api/v1/material/assign/question`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('admin_token')}`
                },
                body: JSON.stringify(formData),
            });

            const data = await res.json();
            if (res.ok) {
                message.success('Upload material successfully');
                setOpenModalUpload(false);
                reloadTable();
            } else {
                notification.error({
                    message: 'Upload material failed',
                    description: data.message,
                });
            }
        } catch (error) {
            notification.error({
                message: 'Upload error',
                description: error instanceof Error ? error.message : 'Cannot connect to server',
            });
        }
    };

    const handleReset = async () => {
        form.resetFields();
        setOpenModalUpload(false);
        setUpload(null);
    }
    const handleDelete = async (id: number) => {
        console.log("id:", id);
        try {
            const res = await fetch(`${API_BASE_URL}/api/v1/material/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('admin_token')}`
                },
            });
            const data = await res.json();
            if (res.ok) {
                message.success('Delete material successfully');
                reloadTable();
                setOpenModalUpload(false);
            } else {
                notification.error({
                    message: 'Delete material failed',
                    description: data.message,
                });
            }
        } catch (error) {
            notification.error({
                message: 'Network error',
                description: 'Cannot connect to server',
            });
        }

    }

    return (
        <>
            <ModalForm
                title={`Uploaded Material`}
                open={openModalUpload}
                onOpenChange={setOpenModalUpload}
                onFinish={submitUpload}
                form={form}
                modalProps={{
                    onCancel: handleReset,
                    afterClose: handleReset,
                    destroyOnClose: true,
                    width: 900,
                    keyboard: false,
                    maskClosable: false,
                    okText: "Upload",
                    cancelText: "Cancel"
                }}
                scrollToFirstError={true}
                preserve={false}
                initialValues={{
                    materType: uploadData?.map(item => item.materType) || '',
                    materLink: uploadData?.map(item => item.materLink) || '',
                }}
            >
                <Col span={24}>
                    <ProCard
                        title="Video Upload"
                        subTitle="Upload video file for listening question"
                        headStyle={{ color: '#d81921' }}
                        style={{ marginBottom: 20 }}
                        headerBordered
                        size="small"
                        bordered
                    >
                        <Form.Item
                            name="videoFile"
                            rules={[{ required: true, message: 'Please upload a video file' }]}
                        >
                            <Upload.Dragger
                                name="file"
                                accept=".mp4,video/mp4"
                                maxCount={1}
                                beforeUpload={(file) => {
                                    const isMP4 = file.type === 'video/mp4';
                                    if (!isMP4) {
                                        message.error('You can only upload MP4 files!');
                                        return Upload.LIST_IGNORE;
                                    }

                                    const isLt100M = file.size / 1024 / 1024 < 100;
                                    if (!isLt100M) {
                                        message.error('Video must be smaller than 100MB!');
                                        return Upload.LIST_IGNORE;
                                    }
                                    return false; // Prevent auto upload
                                }}
                                listType="picture"
                            >
                                <p className="ant-upload-drag-icon">
                                    <VideoCameraOutlined style={{ fontSize: '48px', color: '#1890ff' }} />
                                </p>
                                <p className="ant-upload-text">
                                    Click or drag video file to this area to upload
                                </p>
                                <p className="ant-upload-hint">
                                    Support for single video file upload. Max size: 100MB
                                </p>
                            </Upload.Dragger>
                        </Form.Item>
                    </ProCard>
                </Col>
                <Col span={24}>
                    {uploadData && uploadData.length > 0 ? (
                        <div className="mt-4">
                            <Title level={5}>Uploaded Materials</Title>
                            <Table
                                dataSource={uploadData}
                                columns={[
                                    {
                                        title: 'Type',
                                        dataIndex: 'materType',
                                        key: 'materType',
                                    },
                                    {
                                        title: 'Link',
                                        dataIndex: 'materLink',
                                        key: 'materLink',
                                        render: (text) => (
                                            <a href={text} target="_blank" rel="noopener noreferrer">
                                                {text}
                                            </a>
                                        ),
                                    },
                                    {
                                        title: 'Upload Date',
                                        dataIndex: 'uploadedAt',
                                        key: 'uploadedAt',
                                        render: (text) => new Date(text).toLocaleString(),
                                    },
                                    {
                                        title: 'Action',
                                        key: 'action',
                                        render: (_, record) => (
                                            <Button
                                                type="link"
                                                danger
                                                onClick={() => handleDelete(record.id)}
                                            >
                                                Delete
                                            </Button>
                                        ),
                                    },
                                ]}
                                pagination={false}
                                size="small"
                                bordered
                            />
                        </div>
                    ) : (
                        <Empty description="No materials uploaded yet" />
                    )}
                </Col>
            </ModalForm>
        </>
    )
}
export default ModalUpload;
