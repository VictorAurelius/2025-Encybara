import React, { useEffect, useState } from 'react';
import { API_BASE_URL } from 'service/api.config';
import { useAuth } from 'hooks/useAuth';
import { Spin, Alert, Table } from 'antd';

const CourseLeaderboard: React.FC = () => {
    const { token } = useAuth();
    const [loading, setLoading] = useState(false);
    const [leaderboard, setLeaderboard] = useState<any>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLeaderboard = async () => {
            setLoading(true);
            try {
                const response = await fetch(`${API_BASE_URL}/api/v1/game/leaderboard`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                });
                const data = await response.json();
                if (data.statusCode === 200 && data.data) {
                    setLeaderboard(data.data);
                } else {
                    setError(data.error || "No leaderboard data.");
                }
            } catch (err: any) {
                setError(err.message || "Failed to fetch leaderboard!");
            } finally {
                setLoading(false);
            }
        };
        if (token) fetchLeaderboard();
    }, [token]);

    if (loading) return <Spin />;
    if (error) return <Alert type="error" message={error} />;
    if (!leaderboard) return <Alert type="info" message="No data found." />;

    // Tuỳ theo backend trả về, bạn có thể render như dưới hoặc tuỳ chỉnh lại cột
    return (
        <div>
            <h4>Your Average Score: <b>{leaderboard.userScore ?? 0}</b></h4>
            <h5>Top Scores</h5>
            <Table
                dataSource={leaderboard.topScores.map((score: any, i: number) => ({ key: i + 1, rank: i + 1, score }))}
                columns={[
                    { title: "Rank", dataIndex: "rank" },
                    { title: "Score", dataIndex: "score" }
                ]}
                pagination={false}
                size="small"
            />
        </div>
    );
};

export default CourseLeaderboard;