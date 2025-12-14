import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Tabs, Spin, Alert, App } from 'antd';
import { FileTextOutlined, RocketOutlined, TrophyOutlined } from '@ant-design/icons';
import { useAuth } from 'hooks/useAuth';
import { API_BASE_URL } from 'service/api.config';
import CourseGamesManager from '../courses/components/CourseGamesManager';
import CourseLeaderboard from './components/CourseLeaderboard';

const { TabPane } = Tabs;

interface Course {
    id: number;
    name: string;
    intro: string;
    diffLevel: number;
    recomLevel: number;
    courseType: string;
    speciField: string;
    courseStatus: string;
    group: string;
}

const CourseDetail: React.FC = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const { token } = useAuth();
    const { notification } = App.useApp();

    const [course, setCourse] = useState<Course | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchCourseDetails = async () => {
            if (!courseId) {
                setError("Course ID is missing.");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                const response = await fetch(`${API_BASE_URL}/api/v1/courses/${courseId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Failed to fetch course details');
                }

                const data = await response.json();
                if (data.data) {
                    setCourse(data.data);
                } else {
                    setError("Course data not found.");
                }
            } catch (err: any) {
                console.error("Error fetching course details:", err);
                setError(err.message || "An unexpected error occurred.");
                notification.error({ message: 'Error', description: err.message || 'Failed to fetch course details.' });
            } finally {
                setLoading(false);
            }
        };

        if (token) {
            fetchCourseDetails();
        }
    }, [courseId, token, notification]);

    if (loading) {
        return <div className="p-6 text-center"><Spin size="large" tip="Loading course details..." /></div>;
    }

    if (error) {
        return <div className="p-6"><Alert message="Error" description={error} type="error" showIcon /></div>;
    }

    if (!course) {
        return <div className="p-6"><Alert message="Error" description="Course not found." type="warning" showIcon /></div>;
    }

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold mb-4">Course: {course.name}</h2>
            <p className="text-gray-600 mb-6">{course.intro}</p>

            <Tabs defaultActiveKey="1" className="mt-4">
                <TabPane
                    tab={<span><TrophyOutlined />Leaderboard</span>}
                    key="1"
                >
                    <CourseLeaderboard courseId={course.id} />
                </TabPane>
                <TabPane
                    tab={
                        <span>
                            <RocketOutlined />
                            Games
                        </span>
                    }
                    key="2"
                >
                    <CourseGamesManager courseId={course.id} />
                </TabPane>
            </Tabs>
        </div>
    );
};

export default () => (
    <App>
        <CourseDetail />
    </App>
);
