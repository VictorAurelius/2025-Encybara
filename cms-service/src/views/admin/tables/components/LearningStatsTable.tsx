import React, { useEffect, useState } from "react";
import Card from "components/card";
import { API_BASE_URL } from "service/api.config";
import { useAuth } from "hooks/useAuth";
import { message } from "antd";
import moment from "moment";

interface LearningResult {
    id: number;
    listeningScore: number;
    speakingScore: number;
    readingScore: number;
    writingScore: number;
    lastUpdated: string;
    userId: number;
}

interface User {
    id: number;
    name: string;
    email: string;
}

interface CombinedLearningResult extends LearningResult {
    user: User;
}

interface StatsData {
    date: string;
    count: number;
    totalUsers: number;
    avgReadingScore: number;
    avgListeningScore: number;
    avgSpeakingScore: number;
    avgWritingScore: number;
}

const LearningStatsTable: React.FC = () => {
    const { token } = useAuth();
    const [statsData, setStatsData] = useState<StatsData[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const fetchStatsData = async () => {
            try {
                // Fetch learning results
                const response = await fetch(`${API_BASE_URL}/api/v1/admin/learning-results`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch learning results');
                }

                const data = await response.json();
                const results: LearningResult[] = data.data?.content || [];

                // Fetch users for user info
                const usersResponse = await fetch(`${API_BASE_URL}/api/v1/users`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                const usersData = await usersResponse.json();
                const users: User[] = usersData.result || [];

                // Process data by date
                const statsByDate = processStatsByDate(results, users);
                setStatsData(statsByDate);

            } catch (error) {
                console.error('Error fetching stats data:', error);
                message.error('Failed to load learning statistics');
            } finally {
                setLoading(false);
            }
        };

        if (token) {
            fetchStatsData();
        }
    }, [token]);

    const processStatsByDate = (results: LearningResult[], users: User[]): StatsData[] => {
        const dateMap = new Map<string, LearningResult[]>();

        // Group results by date (YYYY-MM-DD format)
        results.forEach(result => {
            const date = moment(result.lastUpdated).format('YYYY-MM-DD');
            if (!dateMap.has(date)) {
                dateMap.set(date, []);
            }
            dateMap.get(date)!.push(result);
        });

        // Convert to array and sort by date (newest first)
        const statsArray: StatsData[] = Array.from(dateMap.entries())
            .map(([date, dayResults]) => {
                const uniqueUsers = new Set(dayResults.map(r => r.userId));
                const avgReading = dayResults.reduce((sum, r) => sum + r.readingScore, 0) / dayResults.length;
                const avgListening = dayResults.reduce((sum, r) => sum + r.listeningScore, 0) / dayResults.length;
                const avgSpeaking = dayResults.reduce((sum, r) => sum + r.speakingScore, 0) / dayResults.length;
                const avgWriting = dayResults.reduce((sum, r) => sum + r.writingScore, 0) / dayResults.length;

                return {
                    date,
                    count: dayResults.length,
                    totalUsers: uniqueUsers.size,
                    avgReadingScore: avgReading,
                    avgListeningScore: avgListening,
                    avgSpeakingScore: avgSpeaking,
                    avgWritingScore: avgWriting,
                };
            })
            .sort((a, b) => moment(b.date).valueOf() - moment(a.date).valueOf());

        return statsArray;
    };

    const formatScore = (score: number) => {
        return score.toFixed(2);
    };

    if (loading) {
        return (
            <Card extra={"w-full sm:overflow-auto px-6"}>
                <div className="text-center py-8">Loading statistics...</div>
            </Card>
        );
    }

    return (
        <Card extra={"w-full sm:overflow-auto px-6"}>
            <header className="relative flex items-center justify-between pt-4">
                <div className="text-xl font-bold text-navy-700 dark:text-white">
                    Learning Results Statistics by Date
                </div>
            </header>

            <div className="mt-0 overflow-x-scroll xl:overflow-x-hidden">
                <table className="w-full">
                    <thead>
                        <tr className="!border-px !border-gray-400">
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Date</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Total Results</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Active Users</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Avg Reading</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Avg Listening</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Avg Speaking</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Avg Writing</th>
                        </tr>
                    </thead>
                    <tbody>
                        {statsData.map((row, index) => (
                            <tr key={index}>
                                <td className="border-b border-gray-200 py-3 pr-4">
                                    {moment(row.date).format('DD/MM/YYYY')}
                                </td>
                                <td className="border-b border-gray-200 py-3 pr-4 font-semibold">
                                    {row.count}
                                </td>
                                <td className="border-b border-gray-200 py-3 pr-4">
                                    {row.totalUsers}
                                </td>
                                <td className="border-b border-gray-200 py-3 pr-4">
                                    <span style={{
                                        color: row.avgReadingScore < 2.0 ? 'red' :
                                            row.avgReadingScore < 3.5 ? 'orange' : 'green',
                                        fontWeight: 'bold'
                                    }}>
                                        {formatScore(row.avgReadingScore)}
                                    </span>
                                </td>
                                <td className="border-b border-gray-200 py-3 pr-4">
                                    <span style={{
                                        color: row.avgListeningScore < 2.0 ? 'red' :
                                            row.avgListeningScore < 3.5 ? 'orange' : 'green',
                                        fontWeight: 'bold'
                                    }}>
                                        {formatScore(row.avgListeningScore)}
                                    </span>
                                </td>
                                <td className="border-b border-gray-200 py-3 pr-4">
                                    <span style={{
                                        color: row.avgSpeakingScore < 2.0 ? 'red' :
                                            row.avgSpeakingScore < 3.5 ? 'orange' : 'green',
                                        fontWeight: 'bold'
                                    }}>
                                        {formatScore(row.avgSpeakingScore)}
                                    </span>
                                </td>
                                <td className="border-b border-gray-200 py-3 pr-4">
                                    <span style={{
                                        color: row.avgWritingScore < 2.0 ? 'red' :
                                            row.avgWritingScore < 3.5 ? 'orange' : 'green',
                                        fontWeight: 'bold'
                                    }}>
                                        {formatScore(row.avgWritingScore)}
                                    </span>
                                </td>
                            </tr>
                        ))}
                        {statsData.length === 0 && (
                            <tr>
                                <td colSpan={7} className="text-center py-8 text-gray-500">
                                    No statistics data available
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>

            {statsData.length > 0 && (
                <div className="mt-4 text-sm text-gray-600">
                    Total records: {statsData.reduce((sum, day) => sum + day.count, 0)} |
                    Total days: {statsData.length} |
                    Average daily results: {(statsData.reduce((sum, day) => sum + day.count, 0) / statsData.length).toFixed(1)}
                </div>
            )}
        </Card>
    );
};

export default LearningStatsTable;
