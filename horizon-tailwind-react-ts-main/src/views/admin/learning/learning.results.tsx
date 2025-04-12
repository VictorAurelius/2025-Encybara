import React, { useState, useEffect } from "react";
import {
    Card,
    Table,
    Tag,
    Button,
    Modal,
    Descriptions,
    Tabs,
    Empty,
    Select,
    Typography,
    Progress,
    Spin,
    App
} from "antd";
import { useAuth } from "hooks/useAuth";
import { API_BASE_URL } from "service/api.config";
import { SearchOutlined, BarChartOutlined, FileTextOutlined } from "@ant-design/icons";
import type { TableColumnsType } from "antd";
import moment from "moment";
import Access from "../access";
import { formatScore } from "utils/formatvalue";

const { TabPane } = Tabs;
const { Title, Text } = Typography;
const { Option } = Select;

interface LearningResult {
    id: number;
    listeningScore: number;
    speakingScore: number;
    readingScore: number;
    writingScore: number;
    lastUpdated: string;
    previousListeningScore: number;
    previousSpeakingScore: number;
    previousReadingScore: number;
    previousWritingScore: number;
    listeningProgress: number;
    speakingProgress: number;
    readingProgress: number;
    writingProgress: number;
    overallProgress: number;
    userId?: number;
    userName?: string;
}

interface User {
    id: number;
    email: string;
    name: string;
    phone: string | null;
    speciField: string | null;
    avatar: string | null;
    englishlevel: string | null;
}

interface CombinedLearningResult extends LearningResult {
    user: User;
}

const ALL_USERS = -1;

const LearningResults: React.FC = () => {
    const { token } = useAuth();
    const [results, setResults] = useState<CombinedLearningResult[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [detailModalVisible, setDetailModalVisible] = useState<boolean>(false);
    const [selectedResult, setSelectedResult] = useState<CombinedLearningResult | null>(null);
    const [users, setUsers] = useState<User[]>([]);
    const [selectedUserId, setSelectedUserId] = useState<number | undefined>(ALL_USERS);

    const fetchUsers = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/v1/users`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error("Failed to fetch users");
            }

            const data = await response.json();
            const userList = data.result || [];
            setUsers(userList);
            setSelectedUserId(ALL_USERS);
            await fetchLearningResults(ALL_USERS, userList);
        } catch (error) {
            console.error("Error fetching users:", error);
            setLoading(false);
        }
    };

    const fetchLearningResults = async (userId: number, userList: User[] = users) => {
        setLoading(true);
        try {
            const endpoint = userId === ALL_USERS
                ? `${API_BASE_URL}/api/v1/admin/learning-results`
                : `${API_BASE_URL}/api/v1/learning-results/user/${userId}`;

            const response = await fetch(endpoint, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch learning results`);
            }

            const responseData = await response.json();

            if (userId === ALL_USERS) {
                if (responseData && responseData.data.content && Array.isArray(responseData.data.content)) {
                    const combinedResults: CombinedLearningResult[] = await Promise.all(
                        responseData.data.content.map(async (result: LearningResult) => {
                            const user = userList.find(u => u.id === result.userId) || {
                                id: result.userId || 0,
                                name: 'Unknown',
                                email: '',
                                phone: null,
                                speciField: null,
                                avatar: null,
                                englishlevel: null
                            };

                            return {
                                ...result,
                                user
                            };
                        })
                    );

                    setResults(combinedResults);
                } else {
                    setResults([]);
                }
            } else {
                if (responseData && responseData.data) {
                    const selectedUser = userList.find(u => u.id === userId) || {
                        id: userId,
                        name: 'Unknown',
                        email: '',
                        phone: null,
                        speciField: null,
                        avatar: null,
                        englishlevel: null
                    };

                    if (Array.isArray(responseData.data)) {
                        const combinedResults: CombinedLearningResult[] = responseData.data.map((result: LearningResult) => ({
                            ...result,
                            user: selectedUser
                        }));
                        setResults(combinedResults);
                    } else {
                        const combinedResult: CombinedLearningResult = {
                            ...responseData.data,
                            user: selectedUser
                        };
                        setResults([combinedResult]);
                    }
                } else {
                    setResults([]);
                }
            }
        } catch (error) {
            console.error(`Error fetching learning results:`, error);
            setResults([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    useEffect(() => {
        if (selectedUserId !== undefined && selectedUserId !== null) {
            if (users.length > 0) {
                fetchLearningResults(selectedUserId);
            }
        }
    }, [selectedUserId]);

    const handleViewDetail = (result: CombinedLearningResult) => {
        setSelectedResult(result);
        setDetailModalVisible(true);
    };

    const handleUserChange = (userId: number) => {
        setSelectedUserId(userId);
    };

    const columns: TableColumnsType<CombinedLearningResult> = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 70,
        },
        {
            title: 'User',
            dataIndex: ['user', 'name'],
            key: 'userName',
            width: 150,
        },
        {
            title: 'Email',
            dataIndex: ['user', 'email'],
            key: 'email',
            width: 200,
        },
        {
            title: 'Last Updated',
            dataIndex: 'lastUpdated',
            key: 'lastUpdated',
            width: 150,
            render: (text) => moment(text).format('DD/MM/YYYY HH:mm'),
        },
        {
            title: 'Reading Score',
            dataIndex: 'readingScore',
            key: 'readingScore',
            width: 100,
            render: (score) => (
                <span style={{
                    color: score < 2.0 ? 'red' : score < 3.5 ? 'orange' : 'green',
                    fontWeight: 'bold'
                }}>
                    {formatScore(score, 2)}
                </span>
            ),
        },
        {
            title: 'Listening Score',
            dataIndex: 'listeningScore',
            key: 'listeningScore',
            width: 100,
            render: (score) => (
                <span style={{
                    color: score < 2.0 ? 'red' : score < 3.5 ? 'orange' : 'green',
                    fontWeight: 'bold'
                }}>
                    {formatScore(score, 2)}
                </span>
            ),
        },
        {
            title: 'Speaking Score',
            dataIndex: 'speakingScore',
            key: 'speakingScore',
            width: 100,
            render: (score) => (
                <span style={{
                    color: score < 2.0 ? 'red' : score < 3.5 ? 'orange' : 'green',
                    fontWeight: 'bold'
                }}>
                    {formatScore(score, 2)}
                </span>
            ),
        },
        {
            title: 'Writing Score',
            dataIndex: 'writingScore',
            key: 'writingScore',
            width: 100,
            render: (score) => (
                <span style={{
                    color: score < 2.0 ? 'red' : score < 3.5 ? 'orange' : 'green',
                    fontWeight: 'bold'
                }}>
                    {formatScore(score, 2)}
                </span>
            ),
        },
        {
            title: 'Actions',
            key: 'action',
            width: 100,
            render: (_, record) => (
                <Button
                    type="primary"
                    icon={<SearchOutlined />}
                    size="small"
                    onClick={() => handleViewDetail(record)}
                >
                    Details
                </Button>
            ),
        },
    ];

    const renderScoreDetail = () => {
        if (!selectedResult) return null;

        const scoreData = [
            { name: 'Reading', value: selectedResult.readingScore, previousValue: selectedResult.previousReadingScore, progress: selectedResult.readingProgress, color: '#2db7f5' },
            { name: 'Listening', value: selectedResult.listeningScore, previousValue: selectedResult.previousListeningScore, progress: selectedResult.listeningProgress, color: '#87d068' },
            { name: 'Writing', value: selectedResult.writingScore, previousValue: selectedResult.previousWritingScore, progress: selectedResult.writingProgress, color: '#108ee9' },
            { name: 'Speaking', value: selectedResult.speakingScore, previousValue: selectedResult.previousSpeakingScore, progress: selectedResult.speakingProgress, color: '#f50' },
        ];

        return (
            <div className="grid grid-cols-1 gap-4">
                {scoreData.map(item => (
                    <div key={item.name} className="mb-4">
                        <div className="flex justify-between mb-1">
                            <Text strong>{item.name}</Text>
                            <div>
                                <Text strong>{formatScore(item.value, 2)}</Text>
                                {item.progress !== 0 && (
                                    <Text type={item.progress > 0 ? "success" : "danger"} style={{ marginLeft: '8px' }}>
                                        {item.progress > 0 ? `+${formatScore(item.progress, 2)}` : formatScore(item.progress, 2)}
                                    </Text>
                                )}
                            </div>
                        </div>
                        <Progress
                            percent={item.value * 20}
                            status={item.value < 2.0 ? "exception" : "active"}
                            strokeColor={item.color}
                            showInfo={false}
                        />
                        {item.previousValue > 0 && (
                            <div className="mt-1">
                                <Text type="secondary">Previous Score: {formatScore(item.previousValue, 2)}</Text>
                            </div>
                        )}
                    </div>
                ))}
            </div>
        );
    };

    const renderNoData = () => (
        <Empty
            description={
                <span>
                    No learning results available for this user
                </span>
            }
        />
    );

    return (
        <div className="flex flex-col gap-5">
            <Card title="Learning Results Filter" className="w-full">
                <div className="flex flex-wrap gap-4 items-end">
                    <div className="w-64">
                        <Text strong>User</Text>
                        <Select
                            placeholder="Select a user"
                            className="w-full mt-1"
                            value={selectedUserId}
                            onChange={handleUserChange}
                            loading={users.length === 0}
                        >
                            <Option key={ALL_USERS} value={ALL_USERS}>All Users</Option>
                            {users.map(user => (
                                <Option key={user.id} value={user.id}>{user.name} ({user.email})</Option>
                            ))}
                        </Select>
                    </div>
                </div>
            </Card>

            <Card
                title={selectedUserId === ALL_USERS ? "All Learning Results" : "Learning Results"}
                className="w-full"
                extra={<>Total: {results.length}</>}
            >
                {loading ? (
                    <div className="flex justify-center items-center h-64">
                        <Spin size="large" tip="Loading..." />
                    </div>
                ) : results.length > 0 ? (
                    <Table
                        columns={columns}
                        dataSource={results.map(item => ({ ...item, key: item.id }))}
                        pagination={
                            selectedUserId === ALL_USERS && results.length > 10
                                ? {
                                    pageSize: 10,
                                    showSizeChanger: true,
                                    pageSizeOptions: ['10', '20', '50'],
                                    showTotal: (total) => `Total ${total} results`
                                }
                                : false
                        }
                        locale={{ emptyText: renderNoData() }}
                    />
                ) : (
                    renderNoData()
                )}
            </Card>

            <Modal
                key={selectedResult?.id || 'detail-modal'}
                title="Learning Result Details"
                open={detailModalVisible}
                onCancel={() => setDetailModalVisible(false)}
                width={800}
                footer={[
                    <Button key="close" onClick={() => setDetailModalVisible(false)}>Close</Button>
                ]}
            >
                {selectedResult && (
                    <Tabs defaultActiveKey="1">
                        <TabPane
                            tab={
                                <span>
                                    <FileTextOutlined />
                                    Basic Information
                                </span>
                            }
                            key="1"
                        >
                            <Descriptions bordered column={2}>
                                <Descriptions.Item label="Result ID">{selectedResult.id}</Descriptions.Item>
                                <Descriptions.Item label="Last Updated">
                                    {moment(selectedResult.lastUpdated).format('DD/MM/YYYY HH:mm')}
                                </Descriptions.Item>
                                <Descriptions.Item label="User">{selectedResult.user.name}</Descriptions.Item>
                                <Descriptions.Item label="Email">{selectedResult.user.email}</Descriptions.Item>
                                <Descriptions.Item label="English Level">
                                    {selectedResult.user.englishlevel || 'Not specified'}
                                </Descriptions.Item>
                                <Descriptions.Item label="Specialization">
                                    {selectedResult.user.speciField || 'Not specified'}
                                </Descriptions.Item>
                                <Descriptions.Item label="Phone">
                                    {selectedResult.user.phone || 'Not specified'}
                                </Descriptions.Item>
                            </Descriptions>
                        </TabPane>

                        <TabPane
                            tab={
                                <span>
                                    <BarChartOutlined />
                                    Detailed Scores
                                </span>
                            }
                            key="2"
                        >
                            {renderScoreDetail()}
                        </TabPane>
                    </Tabs>
                )}
            </Modal>
        </div>
    );
};

export default () => (
    <Access
        permission={{ module: "CONTENT_MANAGEMENT" }}
    >
        <App>
            <LearningResults />
        </App>
    </Access>
);