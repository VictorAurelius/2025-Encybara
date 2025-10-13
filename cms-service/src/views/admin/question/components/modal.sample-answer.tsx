import React, { useState, useEffect } from 'react';
import { Modal, Table, Button, Space, Popconfirm, message, Form, Input, Select, InputNumber, Drawer, Tag, Typography, Upload, Tooltip } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, EyeOutlined, UploadOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { speakingSampleAnswerService, ResSpeakingSampleAnswerDTO, ReqCreateSpeakingSampleAnswerDTO, ReqUpdateSpeakingSampleAnswerDTO } from '../../../../service/sample.service';
import { IQuestion } from './module.question';

const { TextArea } = Input;
const { Option } = Select;
const { Text, Paragraph } = Typography;

interface ModalSampleAnswerProps {
    openModal: boolean;
    setOpenModal: (open: boolean) => void;
    questionId: number;
    questionData: IQuestion | null;
}

const ModalSampleAnswer: React.FC<ModalSampleAnswerProps> = ({
    openModal,
    setOpenModal,
    questionId,
    questionData
}) => {
    const [sampleAnswers, setSampleAnswers] = useState<ResSpeakingSampleAnswerDTO[]>([]);
    const [loading, setLoading] = useState(false);
    const [drawerVisible, setDrawerVisible] = useState(false);
    const [editingAnswer, setEditingAnswer] = useState<ResSpeakingSampleAnswerDTO | null>(null);
    const [isCreating, setIsCreating] = useState(false);
    const [form] = Form.useForm();

    // Fetch sample answers
    const fetchSampleAnswers = async () => {
        if (!questionId) return;

        setLoading(true);
        try {
            const response = await speakingSampleAnswerService.getSampleAnswersByQuestionId(questionId);
            if (response.statusCode === 200) {
                setSampleAnswers(response.data);
            }
        } catch (error) {
            console.error('Error fetching sample answers:', error);
            message.error('Failed to fetch sample answers');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (openModal && questionId) {
            fetchSampleAnswers();
        }
    }, [openModal, questionId]);

    const handleUploadAudio = async (record: ResSpeakingSampleAnswerDTO, file: File) => {
        try {
            const res = await speakingSampleAnswerService.uploadAudio(record.id, file);
            message.success('Audio file uploaded successfully');
            // Optional: hiển thị link trả về từ API
            // res.data là link file (như ảnh bạn gửi)
            await fetchSampleAnswers();
        } catch (e) {
            console.error(e);
            message.error('Upload audio failed');
        }
    };
    // Handle create sample answer
    const handleCreate = () => {
        setIsCreating(true);
        setEditingAnswer(null);
        form.resetFields();
        setDrawerVisible(true);
    };

    // Handle edit sample answer
    const handleEdit = (record: ResSpeakingSampleAnswerDTO) => {
        setIsCreating(false);
        setEditingAnswer(record);
        form.setFieldsValue({
            answerContent: record.answerContent,
            description: record.description,
            difficultyLevel: record.difficultyLevel,
            estimatedScore: record.estimatedScore,
            audioLink: record.audioLink || ''
        });
        setDrawerVisible(true);
    };

    // Handle delete sample answer
    const handleDelete = async (id: number) => {
        try {
            const response = await speakingSampleAnswerService.deleteSpeakingSampleAnswer(id);
            if (response.statusCode === 200) {
                message.success('Sample answer deleted successfully');
                fetchSampleAnswers();
            }
        } catch (error) {
            console.error('Error deleting sample answer:', error);
            message.error('Failed to delete sample answer');
        }
    };

    // Handle form submit
    const handleSubmit = async (values: any) => {
        try {
            if (isCreating) {
                const createData: ReqCreateSpeakingSampleAnswerDTO = {
                    questionId,
                    answerContent: values.answerContent,
                    description: values.description,
                    difficultyLevel: values.difficultyLevel || 3,
                    estimatedScore: values.estimatedScore || 0
                };
                const response = await speakingSampleAnswerService.createSpeakingSampleAnswer(createData);
                // Nếu người dùng nhập audioLink -> gọi API riêng
                if (values.audioLink && response.data?.id) {
                    await speakingSampleAnswerService.updateAudioLink(response.data.id, values.audioLink);
                }
                if (response.statusCode === 200) {
                    message.success('Sample answer created successfully');
                    setDrawerVisible(false);
                    fetchSampleAnswers();
                }
            } else if (editingAnswer) {
                const updateData: ReqUpdateSpeakingSampleAnswerDTO = {
                    id: editingAnswer.id,
                    questionId,
                    answerContent: values.answerContent,
                    description: values.description,
                    difficultyLevel: values.difficultyLevel,
                    estimatedScore: values.estimatedScore
                };
                const response = await speakingSampleAnswerService.updateSpeakingSampleAnswer(updateData);
                // Bỏ logic update audio khi update sample (theo yêu cầu)
                if (response.statusCode === 200) {
                    message.success('Sample answer updated successfully');
                    setDrawerVisible(false);
                    fetchSampleAnswers();
                }
            }
        } catch (error) {
            console.error('Error saving sample answer:', error);
            message.error('Failed to save sample answer');
        }
    };

    const columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 60,
        },
        {
            title: 'Answer Content',
            dataIndex: 'answerContent',
            key: 'answerContent',
            render: (text: string) => (
                <Paragraph
                    ellipsis={{ rows: 2, expandable: true, symbol: 'more' }}
                    style={{ margin: 0, maxWidth: 300 }}
                >
                    {text}
                </Paragraph>
            ),
        },
        {
            title: 'Difficulty',
            dataIndex: 'difficultyLevel',
            key: 'difficultyLevel',
            width: 100,
            render: (level: number) => {
                const colors = { 1: 'red', 2: 'orange', 3: 'blue', 4: 'green', 5: 'purple' };
                return (
                    <Tag color={colors[level as keyof typeof colors]}>
                        {speakingSampleAnswerService.getDifficultyLevelText(level)}
                    </Tag>
                );
            },
        },
        {
            title: 'Score',
            dataIndex: 'estimatedScore',
            key: 'estimatedScore',
            width: 100,
            render: (score: number) => (
                <Tag color={score >= 80 ? 'green' : score >= 60 ? 'orange' : 'red'}>
                    {score}/100
                </Tag>
            ),
        },
        {
            title: 'Description',
            dataIndex: 'description',
            key: 'description',
            render: (text: string) => (
                <Text ellipsis style={{ maxWidth: 200 }}>
                    {text || 'No description'}
                </Text>
            ),
        },
        {
            title: 'Audio',
            dataIndex: 'audioLink',
            key: 'audioLink',
            width: 280,
            render: (link: string, record: ResSpeakingSampleAnswerDTO) => {
                const src = speakingSampleAnswerService.getPlayableAudioUrl(link);
                return src ? (
                    <audio controls src={src} style={{ width: 260 }} />
                ) : (
                    <Typography.Text type="secondary">No audio</Typography.Text>
                );
            },
        },
        {
            title: 'Actions',
            key: 'actions',
            width: 170,
            render: (_: any, record: ResSpeakingSampleAnswerDTO) => (
                <Space>
                    <Button type="text" icon={<EditOutlined />} onClick={() => handleEdit(record)} size="small" />
                    <Popconfirm
                        title="Are you sure you want to delete this sample answer?"
                        onConfirm={() => handleDelete(record.id)}
                        okText="Yes"
                        cancelText="No"
                    >
                        <Button type="text" danger icon={<DeleteOutlined />} size="small" />
                    </Popconfirm>

                    {/* Nút Upload audio */}
                    <Upload
                        accept="audio/*"
                        showUploadList={false}
                        beforeUpload={(file) => {
                            handleUploadAudio(record, file as File);
                            return false; // chặn upload mặc định
                        }}
                    >
                        <Button type="text" icon={<UploadOutlined />} size="small" />
                    </Upload>
                </Space>
            ),
        },
    ];

    return (
        <>
            <Modal
                title={`Sample Answers - ${questionData?.quesContent || 'Question'}`}
                open={openModal}
                onCancel={() => setOpenModal(false)}
                width={1200}
                footer={[
                    <Button key="add" type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                        Add Sample Answer
                    </Button>,
                    <Button key="close" onClick={() => setOpenModal(false)}>
                        Close
                    </Button>
                ]}
            >
                <Table
                    columns={columns}
                    dataSource={sampleAnswers}
                    loading={loading}
                    rowKey="id"
                    pagination={{
                        pageSize: 10,
                        showSizeChanger: true,
                        showQuickJumper: true,
                    }}
                    scroll={{ x: 800 }}
                />
            </Modal>

            <Drawer
                title={isCreating ? 'Create Sample Answer' : 'Edit Sample Answer'}
                open={drawerVisible}
                onClose={() => setDrawerVisible(false)}
                width={600}
                footer={[
                    <Button key="cancel" onClick={() => setDrawerVisible(false)}>
                        Cancel
                    </Button>,
                    <Button key="submit" type="primary" onClick={() => form.submit()}>
                        {isCreating ? 'Create' : 'Update'}
                    </Button>
                ]}
            >
                <Form
                    form={form}
                    layout="vertical"
                    onFinish={handleSubmit}
                    initialValues={{
                        difficultyLevel: 3,
                        estimatedScore: 0
                    }}
                >
                    {/* Audio preview & delete when editing */}
                    {!isCreating && editingAnswer?.audioLink ? (
                        <Form.Item label="Current Audio">
                            <Space align="center">
                                <audio
                                    controls
                                    src={speakingSampleAnswerService.getPlayableAudioUrl(editingAnswer.audioLink)}
                                    style={{ width: 360 }}
                                />
                                <Popconfirm
                                    title="Remove audio from this sample?"
                                    okText="Yes"
                                    cancelText="No"
                                    onConfirm={async () => {
                                        try {
                                            await speakingSampleAnswerService.deleteAudio(editingAnswer.id);
                                            message.success('Audio removed');
                                            // update local form/display state + reload list
                                            setEditingAnswer({ ...editingAnswer, audioLink: undefined });
                                            await fetchSampleAnswers();
                                        } catch (e) {
                                            message.error('Failed to remove audio');
                                        }
                                    }}
                                >
                                    <Tooltip title="Remove audio">
                                        <Button type="default" icon={<CloseCircleOutlined />} danger />
                                    </Tooltip>
                                </Popconfirm>
                            </Space>
                        </Form.Item>
                    ) : null}
                    <Form.Item
                        name="answerContent"
                        label="Answer Content"
                        rules={[{ required: true, message: 'Please enter answer content' }]}
                    >
                        <TextArea
                            rows={4}
                            placeholder="Enter the sample answer content..."
                            disabled={!isCreating}
                        />
                    </Form.Item>

                    <Form.Item
                        name="description"
                        label="Description"
                    >
                        <TextArea
                            rows={2}
                            placeholder="Enter description (optional)..."
                        />
                    </Form.Item>

                    <Form.Item
                        name="difficultyLevel"
                        label="Difficulty Level"
                        rules={[{ required: true, message: 'Please select difficulty level' }]}
                    >
                        <Select placeholder="Select difficulty level">
                            <Option value={1}>Basic (1)</Option>
                            <Option value={2}>Elementary (2)</Option>
                            <Option value={3}>Intermediate (3)</Option>
                            <Option value={4}>Advanced (4)</Option>
                            <Option value={5}>Expert (5)</Option>
                        </Select>
                    </Form.Item>

                    <Form.Item
                        name="estimatedScore"
                        label="Estimated Score"
                        rules={[
                            { required: true, message: 'Please enter estimated score' },
                            { type: 'number', min: 0, max: 100, message: 'Score must be between 0 and 100' }
                        ]}
                    >
                        <InputNumber
                            min={0}
                            max={100}
                            style={{ width: '100%' }}
                            placeholder="Enter estimated score (0-100)"
                        />
                    </Form.Item>
                    {/* Bỏ field chỉnh audioLink khi update; vẫn cho nhập khi tạo mới nếu cần */}
                    {isCreating ? (
                        <Form.Item name="audioLink" label="Audio Link">
                            <Input placeholder="Paste audio URL here (optional)" />
                        </Form.Item>
                    ) : null}
                </Form>
            </Drawer>
        </>
    );
};

export default ModalSampleAnswer;
