import React, { useEffect, useState } from 'react';
import ApiService from 'service/api.service';
import { useAuth } from 'hooks/useAuth';
import { Spin, Alert, Table } from 'antd';

interface CourseLeaderboardProps {
    courseId?: number | string; // Course ID to fetch leaderboard for specific course
}

const CourseLeaderboard: React.FC<CourseLeaderboardProps> = ({ courseId }) => {
    const api = ApiService();
    const { token } = useAuth();
    const [loading, setLoading] = useState(false);
    const [leaderboard, setLeaderboard] = useState<any>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLeaderboard = async () => {
            setLoading(true);
            try {
                // Build URL based on courseId
                let url = '/api/v1/game/leaderboard';
                if (courseId) {
                    url = `/api/v1/game/leaderboard/course/${courseId}`;
                }

                const data = await api.get(url);

                // ApiService returns response.data directly; ensure we have the wrapper
                if (data && data.statusCode === 200 && data.data) {
                    setLeaderboard(data.data);
                } else if (data && data.data) {
                    setLeaderboard(data.data);
                } else {
                    setError((data && data.error) || 'No leaderboard data.');
                }
            } catch (err: any) {
                setError(err?.message || 'Failed to fetch leaderboard!');
            } finally {
                setLoading(false);
            }
        };

        // Fetch if token available or if courseId provided
        if (token || courseId) fetchLeaderboard();
    }, [token, courseId]);

    if (loading) return <Spin />;
    if (error) return <Alert type="error" message={error} />;
    if (!leaderboard) return <Alert type="info" message="No data found." />;

    return (
        <div>
            <h3>Course Leaderboard: <b>{leaderboard.courseName}</b></h3>
            <Table
                dataSource={leaderboard.topScores || []}
                columns={[
                    { 
                        title: "Rank", 
                        dataIndex: "rank",
                        width: 80
                    },
                    { 
                        title: "User", 
                        dataIndex: "userName",
                        width: 150
                    },
                    { 
                        title: "Score", 
                        dataIndex: "score",
                        width: 100,
                        sorter: (a: any, b: any) => b.score - a.score
                    },
                    { 
                        title: "Accuracy (%)", 
                        dataIndex: "accuracy",
                        width: 120,
                        render: (accuracy: number) => `${accuracy?.toFixed(1) || 0}%`
                    },
                    { 
                        title: "Game", 
                        dataIndex: "gameName",
                        ellipsis: true
                    }
                ]}
                pagination={false}
                size="small"
                rowKey={(record, index) => index}
            />
        </div>
    );
};

export default CourseLeaderboard;