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

// Định nghĩa interfaces cho dữ liệu
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
    userId?: number; // Thêm để lưu trữ tạm thời
    userName?: string; // Thêm để lưu trữ tạm thời
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

const ALL_USERS = -1; // Giá trị đặc biệt để đánh dấu lựa chọn "Tất cả người dùng"

const LearningResults: React.FC = () => {
    const { token } = useAuth();
    const [results, setResults] = useState<CombinedLearningResult[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [detailModalVisible, setDetailModalVisible] = useState<boolean>(false);
    const [selectedResult, setSelectedResult] = useState<CombinedLearningResult | null>(null);
    const [users, setUsers] = useState<User[]>([]);
    const [selectedUserId, setSelectedUserId] = useState<number | undefined>(ALL_USERS);

    // Lấy danh sách người dùng
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
            setUsers(userList); // Lưu danh sách người dùng

            // Đặt giá trị mặc định là "Tất cả người dùng"
            setSelectedUserId(ALL_USERS);

            // Quan trọng: truyền userList vào hàm fetchLearningResults để sử dụng dữ liệu mới nhất
            await fetchLearningResults(ALL_USERS, userList);
        } catch (error) {
            console.error("Error fetching users:", error);
            setLoading(false);
        }
    };

    // Lấy kết quả học tập của một người dùng cụ thể
    const fetchLearningResults = async (userId: number, userList: User[] = users) => {
        setLoading(true);
        try {
            // Chọn endpoint dựa vào việc đã chọn tất cả người dùng hay một người dùng cụ thể
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
            console.log("Dữ liệu:", responseData);

            if (userId === ALL_USERS) {
                // Xử lý trường hợp lấy tất cả kết quả
                if (responseData && responseData.data.content && Array.isArray(responseData.data.content)) {
                    const combinedResults: CombinedLearningResult[] = await Promise.all(
                        responseData.data.content.map(async (result: LearningResult) => {
                            // Sử dụng userList thay vì users state để đảm bảo dữ liệu mới nhất
                            const user = userList.find(u => u.id === result.userId) || {
                                id: result.userId || 0,
                                name: 'Không xác định',
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
                    // Tìm thông tin người dùng được chọn
                    const selectedUser = userList.find(u => u.id === userId) || {
                        id: userId,
                        name: 'Không xác định',
                        email: '',
                        phone: null,
                        speciField: null,
                        avatar: null,
                        englishlevel: null
                    };

                    // Xử lý API trả về một mảng kết quả
                    if (Array.isArray(responseData.data)) {
                        const combinedResults: CombinedLearningResult[] = responseData.data.map((result: LearningResult) => ({
                            ...result,
                            user: selectedUser
                        }));
                        setResults(combinedResults);
                    }
                    // Hoặc API trả về một đối tượng kết quả duy nhất
                    else {
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
    // Cập nhật useEffect khi selectedUserId thay đổi
    useEffect(() => {
        if (selectedUserId !== undefined && selectedUserId !== null) {
            // Chỉ gọi fetch khi không phải lần đầu tiên (từ fetchUsers đã gọi)
            if (users.length > 0) {
                fetchLearningResults(selectedUserId);
            }
        }
    }, [selectedUserId]);





    // Xem chi tiết kết quả học tập
    const handleViewDetail = (result: CombinedLearningResult) => {
        setSelectedResult(result);
        setDetailModalVisible(true);
    };

    // Cập nhật người dùng được chọn
    const handleUserChange = (userId: number) => {
        setSelectedUserId(userId);
    };

    // Định nghĩa cột cho bảng
    const columns: TableColumnsType<CombinedLearningResult> = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 70,
        },
        {
            title: 'Người dùng',
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
            title: 'Ngày cập nhật',
            dataIndex: 'lastUpdated',
            key: 'lastUpdated',
            width: 150,
            render: (text) => moment(text).format('DD/MM/YYYY HH:mm'),
        },
        {
            title: 'Điểm đọc',
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
            title: 'Điểm nghe',
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
            title: 'Điểm nói',
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
            title: 'Điểm viết',
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
        }
        ,
        {
            title: 'Hành động',
            key: 'action',
            width: 100,
            render: (_, record) => (
                <Button
                    type="primary"
                    icon={<SearchOutlined />}
                    size="small"
                    onClick={() => handleViewDetail(record)}
                >
                    Chi tiết
                </Button>
            ),
        },
    ];

    // Hiển thị chi tiết điểm số dạng thanh tiến trình
    const renderScoreDetail = () => {
        if (!selectedResult) return null;

        const scoreData = [
            { name: 'Đọc', value: selectedResult.readingScore, previousValue: selectedResult.previousReadingScore, progress: selectedResult.readingProgress, color: '#2db7f5' },
            { name: 'Nghe', value: selectedResult.listeningScore, previousValue: selectedResult.previousListeningScore, progress: selectedResult.listeningProgress, color: '#87d068' },
            { name: 'Viết', value: selectedResult.writingScore, previousValue: selectedResult.previousWritingScore, progress: selectedResult.writingProgress, color: '#108ee9' },
            { name: 'Nói', value: selectedResult.speakingScore, previousValue: selectedResult.previousSpeakingScore, progress: selectedResult.speakingProgress, color: '#f50' },
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
                            percent={item.value * 20} // Chuyển đổi thang điểm 0-5 sang 0-100
                            status={item.value < 2.0 ? "exception" : "active"}
                            strokeColor={item.color}
                            showInfo={false}
                        />
                        {item.previousValue > 0 && (
                            <div className="mt-1">
                                <Text type="secondary">Điểm trước đó: {formatScore(item.previousValue, 2)}</Text>
                            </div>
                        )}
                    </div>
                ))}

            </div>
        );
    };

    // Nội dung hiển thị khi không có dữ liệu
    const renderNoData = () => (
        <Empty
            description={
                <span>
                    Không có dữ liệu kết quả học tập cho người dùng này
                </span>
            }
        />
    );

    return (



        <div className="flex flex-col gap-5">
            <Card title="Bộ lọc kết quả học tập" className="w-full">
                <div className="flex flex-wrap gap-4 items-end">
                    <div className="w-64">
                        <Text strong>Người dùng</Text>
                        <Select
                            placeholder="Chọn người dùng"
                            className="w-full mt-1"
                            value={selectedUserId}
                            onChange={handleUserChange}
                            loading={users.length === 0}
                        >
                            <Option key={ALL_USERS} value={ALL_USERS}>Tất cả người dùng</Option>
                            {users.map(user => (
                                <Option key={user.id} value={user.id}>{user.name} ({user.email})</Option>
                            ))}
                        </Select>
                    </div>
                </div>
            </Card>

            <Card
                title={selectedUserId === ALL_USERS ? "Tất cả kết quả học tập" : "Kết quả học tập"}
                className="w-full"
                extra={<>Tổng số: {results.length}</>}
            >
                {loading ? (
                    <div className="flex justify-center items-center h-64">
                        <Spin size="large" tip="Đang tải..." />
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
                                    showTotal: (total) => `Tổng ${total} kết quả`
                                }
                                : false
                        }
                        locale={{ emptyText: renderNoData() }}
                    />
                ) : (
                    renderNoData()
                )}
            </Card>

            {/* Modal chi tiết kết quả */}
            <Modal
                key={selectedResult?.id || 'detail-modal'}
                title="Chi tiết kết quả học tập"
                open={detailModalVisible}
                onCancel={() => setDetailModalVisible(false)}
                width={800}
                footer={[
                    <Button key="close" onClick={() => setDetailModalVisible(false)}>Đóng</Button>
                ]}
            >
                {selectedResult && (
                    <Tabs defaultActiveKey="1">
                        <TabPane
                            tab={
                                <span>
                                    <FileTextOutlined />
                                    Thông tin cơ bản
                                </span>
                            }
                            key="1"
                        >
                            <Descriptions bordered column={2}>
                                <Descriptions.Item label="ID kết quả">{selectedResult.id}</Descriptions.Item>
                                <Descriptions.Item label="Ngày cập nhật">
                                    {moment(selectedResult.lastUpdated).format('DD/MM/YYYY HH:mm')}
                                </Descriptions.Item>
                                <Descriptions.Item label="Người dùng">{selectedResult.user.name}</Descriptions.Item>
                                <Descriptions.Item label="Email">{selectedResult.user.email}</Descriptions.Item>
                                <Descriptions.Item label="Trình độ tiếng Anh">
                                    {selectedResult.user.englishlevel || 'Chưa xác định'}
                                </Descriptions.Item>
                                <Descriptions.Item label="Chuyên ngành">
                                    {selectedResult.user.speciField || 'Chưa xác định'}
                                </Descriptions.Item>
                                <Descriptions.Item label="Số điện thoại">
                                    {selectedResult.user.phone || 'Chưa xác định'}
                                </Descriptions.Item>
                            </Descriptions>
                        </TabPane>

                        <TabPane
                            tab={
                                <span>
                                    <BarChartOutlined />
                                    Điểm số chi tiết
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