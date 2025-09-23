import React from 'react';
import { Modal, List, Card, Tag, Typography, Divider } from 'antd';
import { RiseOutlined, TrophyOutlined, StarOutlined } from '@ant-design/icons';
import { formatScore } from 'utils/formatvalue';
const { Title, Text } = Typography;

export interface StudyResult {
    id: number;
    diffLevel: number;    // Thời gian học (giây)
    comLevel: number;   // Mức độ hoàn thành
    totalPoints: number; // Tổng điểm
    sessionId: number;
}

interface ResultModalProps {
    isOpen: boolean;
    onClose: () => void;
    results: StudyResult[];
    // lessonName: string;
}

const ResultModal: React.FC<ResultModalProps> = ({
    isOpen,
    onClose,
    results
    // lessonName
}) => {
    // Hàm chuyển đổi thời gian từ giây sang định dạng phút:giây
    const formatTime = (seconds: number) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
    };

    // Hàm tính toán màu sắc dựa trên điểm số
    const getScoreColor = (score: number) => {
        if (score >= 8) return 'green';
        if (score >= 6) return 'blue';
        if (score >= 4) return 'orange';
        return 'red';
    };

    return (
        <Modal
            title={<Title level={4}>Learning Results </Title>}
            open={isOpen}
            onCancel={onClose}
            footer={null}
            width={600}
        >
            <List
                dataSource={results}
                renderItem={(result, index) => (
                    <Card
                        className="mb-4 hover:shadow-md transition-all duration-300"
                        key={result.id}
                    >
                        <div className="flex justify-between items-center mb-2">
                            <Tag color="purple" className="text-base">
                                Attempt #{index + 1}
                            </Tag>
                            <Text type="secondary">ID: {result.id}</Text>
                        </div>

                        <Divider className="my-3" />

                        <div className="grid grid-cols-3 gap-4">
                            {/* Thời gian học */}
                            <div className="flex flex-col items-center">
                                <RiseOutlined className="text-xl text-red-500 mb-2" />
                                <Text strong>Difficulty Level</Text>
                                <Text>{formatScore(result.diffLevel, 2)}</Text>
                            </div>

                            {/* Mức độ hoàn thành */}
                            <div className="flex flex-col items-center">
                                <StarOutlined className="text-xl text-yellow-500 mb-2" />
                                <Text strong>Completion</Text>
                                <Text>{formatScore(result.comLevel, 1)}%</Text>
                            </div>

                            {/* Tổng điểm */}
                            <div className="flex flex-col items-center">
                                <TrophyOutlined className="text-xl text-green-500 mb-2" />
                                <Text strong>Total Points</Text>
                                <Tag color={getScoreColor(result.totalPoints)} className="mt-1">
                                    {formatScore(result.totalPoints, 2)} points
                                </Tag>
                            </div>
                        </div>
                    </Card>
                )}
                locale={{
                    emptyText: (
                        <div className="text-center py-8">
                            <Text type="secondary">No results available</Text>
                        </div>
                    )
                }}
            />
        </Modal>
    );
};

export default ResultModal;
