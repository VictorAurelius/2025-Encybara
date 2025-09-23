import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import UserProfile from "./components/UserProfile";
import { API_BASE_URL } from "service/api.config";
import { useAuth } from "hooks/useAuth";
import Access from "../access";
import { App } from 'antd';
const UserProfilePage: React.FC = () => {
    const { message, notification } = App.useApp();
    const { token } = useAuth();
    const { userId } = useParams<{ userId: string }>();
    const [userData, setUserData] = useState<any>(null);
    const [coursesData, setCoursesData] = useState<any>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [reviewsData, setReviewsData] = useState<any>([]);
    const [scheduleData, setScheduleData] = useState<any>([]);

    useEffect(() => {
        console.log("Fetched User ID:", userId);

        if (!userId) {
            console.error("User ID is undefined or empty");
            setError("Invalid User ID");
            setLoading(false);
            return;
        }

        const fetchUserData = async () => {
            try {
                const userResponse = await fetch(`${API_BASE_URL}/api/v1/users/${userId}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });
                const userData = await userResponse.json();

                console.log("User Data:", userData);
                const getCourses = await fetch(`${API_BASE_URL}/api/v1/enrollments/user/${userId}?page=1&size=100`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });
                const getCoursesData = await getCourses.json();

                const courseIds = getCoursesData.data.content.map((item: any) => item.courseId);
                const courseDetails = await Promise.all(
                    courseIds.map(async (id: number) => {
                        const courseResponse = await fetch(`${API_BASE_URL}/api/v1/courses/${id}`, {
                            method: 'GET',
                            headers: {
                                'Authorization': `Bearer ${token}`,
                                'Content-Type': 'application/json',
                            },
                        });
                        const courseData = await courseResponse.json();
                        return courseData.data;
                    })
                );

                console.log("Course Details:", courseDetails);

                const reviewData = await fetch(`${API_BASE_URL}/api/v1/reviews/user/${userId}?page=1&size=100`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });
                const revdata = await reviewData.json();

                const scheduleData = await fetch(`${API_BASE_URL}/api/v1/schedules/user/${userId}?page=1&size=100`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });
                const scheData = await scheduleData.json();
                setScheduleData(scheData.data.content);
                setReviewsData(revdata.data.content);
                // Cập nhật state với dữ liệu nhận được
                setUserData(userData);
                setCoursesData(courseDetails || []);
            } catch (error) {
                console.error("Error fetching data:", error);
                setError("Failed to fetch data");
            } finally {
                setLoading(false);
            }
        };

        if (token) {
            fetchUserData();
        }
    }, [token, userId]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    console.log("User Data before passing to UserProfile:", userData, coursesData);

    return (
        <div className="p-6">

            <UserProfile user={userData} courses={coursesData} reviews={reviewsData} schedules={scheduleData} />


        </div>
    );
};

export default () => (
    <App>
        <UserProfilePage />
    </App>
);
