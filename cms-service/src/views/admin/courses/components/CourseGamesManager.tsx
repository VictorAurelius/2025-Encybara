import React, { useEffect, useState } from 'react';
import { Button, Table, Modal, Form, Input, Select, InputNumber, notification, Popconfirm, Space } from 'antd';
import { API_BASE_URL } from 'service/api.config';
import { useAuth } from 'hooks/useAuth';

const { Option } = Select;

interface Game {
    id: number;
    name: string;
    description: string;
    gameType: 'REVIEW' | 'PRACTICE' | 'CHALLENGE'; // Cần match với enum ở backend
    maxQuestions: number;
    timeLimit: number; // In minutes
    // Thêm các trường nội dung game nếu có, ví dụ:
    // flashcards?: Array<{ term: string, definition: string }>;
    // questions?: Array<{ questionText: string, options: string[], correctAnswer: string }>;
}

interface CourseGamesManagerProps {
    courseId: number;
}

const CourseGamesManager: React.FC<CourseGamesManagerProps> = ({ courseId }) => {
    const { token } = useAuth();
    const [games, setGames] = useState<Game[]>([]);
    const [loading, setLoading] = useState(false);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingGame, setEditingGame] = useState<Game | null>(null);
    const [form] = Form.useForm();

    const fetchGames = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/v1/game/course/${courseId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch games');
            }
            const data = await response.json();
            setGames(data.data || []);
        } catch (error) {
            console.error('Error fetching games:', error);
            notification.error({ message: 'Error', description: 'Failed to load games.' });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (courseId) {
            fetchGames();
        }
    }, [courseId, token]);

    const handleAddGame = () => {
        setEditingGame(null);
        form.resetFields();
        setIsModalVisible(true);
    };

    const handleEditGame = (game: Game) => {
        setEditingGame(game);
        form.setFieldsValue(game); // Điền dữ liệu game vào form
        setIsModalVisible(true);
    };

    const handleDeleteGame = async (gameId: number) => {
        setLoading(true);
        try {
            // TODO: API DELETE này cần được thêm vào GameControllerV2.java nếu chưa có
            // Tham khảo: DELETE /api/v1/game/{gameId}
            const response = await fetch(`${API_BASE_URL}/api/v1/game/${gameId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to delete game');
            }

            notification.success({ message: 'Success', description: 'Game deleted successfully.' });
            fetchGames(); // Refresh list
        } catch (error: any) {
            console.error('Error deleting game:', error);
            notification.error({ message: 'Error', description: error.message || 'Failed to delete game.' });
        } finally {
            setLoading(false);
        }
    };

    const handleSaveGame = async (values: any) => {
        setLoading(true);
        try {
            const method = editingGame ? 'PUT' : 'POST';
            // Backend API createGame hiện tại yêu cầu `courseId` trong body, không phải path variable
            // API PUT cần có gameId trong URL
            const url = editingGame
                ? `${API_BASE_URL}/api/v1/game/${editingGame.id}`
                : `${API_BASE_URL}/api/v1/game/create`;

            const payload = {
                ...values,
                courseId: courseId, // Đảm bảo courseId được gửi
            };

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to save game');
            }

            notification.success({ message: 'Success', description: 'Game saved successfully.' });
            fetchGames(); // Refresh list
            setIsModalVisible(false);
        } catch (error: any) {
            console.error('Error saving game:', error);
            notification.error({ message: 'Error', description: error.message || 'Failed to save game.' });
        } finally {
            setLoading(false);
        }
    };

    const columns = [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 70 },
        { title: 'Game Name', dataIndex: 'name', key: 'name' },
        { title: 'Description', dataIndex: 'description', key: 'description' },
        { title: 'Game Type', dataIndex: 'gameType', key: 'gameType' },
        { title: 'Max Questions', dataIndex: 'maxQuestions', key: 'maxQuestions' },
        { title: 'Time Limit (min)', dataIndex: 'timeLimit', key: 'timeLimit' },
        {
            title: 'Actions',
            key: 'actions',
            width: 150,
            render: (_: any, record: Game) => (
                <Space>
                    <Button type="link" onClick={() => handleEditGame(record)}>Edit</Button>
                    <Popconfirm
                        title="Are you sure to delete this game?"
                        onConfirm={() => handleDeleteGame(record.id)}
                        okText="Yes"
                        cancelText="No"
                    >
                        <Button type="link" danger>Delete</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    // TODO: Conditional rendering for game content based on gameType
    const renderGameContentForm = (gameType: 'FLASHCARD' | 'QUIZ' | 'MATCHING' | undefined) => {
        switch (gameType) {
            case 'FLASHCARD':
                return (
                    <div>
                        <h4>Flashcard Configuration</h4>
                        <p>This section would contain fields to add/edit flashcards.</p>
                        {/* Ví dụ: Thêm các input cho Term, Definition, Image, Audio */}
                        {/* Có thể dùng Form.List để quản lý nhiều flashcard */}
                    </div>
                );
            case 'QUIZ':
                return (
                    <div>
                        <h4>Quiz Configuration</h4>
                        <p>This section would contain fields to add/edit quiz questions and answers.</p>
                        {/* Ví dụ: Thêm input cho Question, Options, Correct Answer */}
                        {/* Có thể dùng Form.List để quản lý nhiều câu hỏi */}
                    </div>
                );
            case 'MATCHING':
                return (
                    <div>
                        <h4>Matching Configuration</h4>
                        <p>This section would contain fields to add/edit matching pairs.</p>
                    </div>
                );
            default:
                return <p>Select a Game Type to configure its content.</p>;
        }
    };

    return (
        <div>
            <div className="flex justify-end mb-4">
                <Button type="primary" onClick={handleAddGame}>Add New Game</Button>
            </div>
            <Table
                columns={columns}
                dataSource={games}
                rowKey="id"
                loading={loading}
                pagination={false}
            />

            <Modal
                title={editingGame ? 'Edit Game' : 'Create New Game'}
                open={isModalVisible}
                onCancel={() => setIsModalVisible(false)}
                footer={null}
                width={700}
            >
                <Form form={form} layout="vertical" onFinish={handleSaveGame}>
                    <Form.Item name="name" label="Game Name" rules={[{ required: true, message: 'Please enter game name!' }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item name="description" label="Description">
                        <Input.TextArea />
                    </Form.Item>
                    <Form.Item name="gameType" label="Game Type" rules={[{ required: true, message: 'Please select game type!' }]}>
                        <Select placeholder="Select a game type">
                            <Option value="PRACTICE">Practice</Option>
                            <Option value="REVIEW">Review</Option>
                            <Option value="CHALLENGE">Challenge</Option>
                            {/* Thêm các loại game khác nếu có */}
                        </Select>
                    </Form.Item>
                    <Form.Item name="maxQuestions" label="Max Questions" rules={[{ required: true, message: 'Please enter max questions!' }]}>
                        <InputNumber min={1} style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item name="timeLimit" label="Time Limit (minutes)" rules={[{ required: true, message: 'Please enter time limit!' }]}>
                        <InputNumber min={1} style={{ width: '100%' }} />
                    </Form.Item>

            
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={loading}>
                            {editingGame ? 'Update Game' : 'Create Game'}
                        </Button>
                        <Button onClick={() => setIsModalVisible(false)} style={{ marginLeft: 8 }}>
                            Cancel
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default CourseGamesManager;
