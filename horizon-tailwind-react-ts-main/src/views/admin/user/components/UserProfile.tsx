import React, { useEffect, useState } from "react";
import {
    useReactTable,
    getCoreRowModel,
    ColumnDef,
    flexRender,
} from "@tanstack/react-table";
import { useAuth } from "hooks/useAuth";
import { API_BASE_URL } from "service/api.config";
import { List, Card, Button, notification, message } from "antd";
import ResultModal, { StudyResult } from './module.result';

interface Review {
    id: number;
    userId: number;
    courseId: number;
    reContent: string;
    reSubject: string;
    numStar: number;
    numLike: number;
    status: string;
};
interface Schedule {
    id: number;
    userId: number;
    courseId: number;
    isDaily: boolean;
    scheduleTime: string;
}

type UserProfileProps = {
    user: {
        id: number;
        name: string;
        email: string;
        speciField: string;
        englishlevel: string;
    };
    courses: {
        id: number;
        name: string;
        intro: string;
        diffLevel: number;
        recomLevel: number;
        courseType: string;
        speciField: string;
    }[];
    reviews: Review[];
    schedules: Schedule[];
};

const UserProfile: React.FC<UserProfileProps> = ({ user, courses, reviews, schedules }) => {
    const { token } = useAuth();
    const [lessons, setLessons] = useState<any[]>([]);
    const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
    const [courseDetails, setCourseDetails] = useState<any[]>([]);
    const [scheduleDetails, setScheduleDetails] = useState<any[]>([]);
    const [isResultModalOpen, setIsResultModalOpen] = useState(false);
    const [selectedLessonResults, setSelectedLessonResults] = useState<StudyResult[]>([]);
    const handleViewResults = async (lesson: any) => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/v1/lesson-results/user/${user.id}/lesson/${lesson.id}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!res.ok) {
                throw new Error('Failed to fetch results');
            }

            const data = await res.json();
            console.log("data:", data.data.content);
            setSelectedLessonResults(data.data.content);
            setIsResultModalOpen(true);
        } catch (error) {
            console.error("Error fetching lesson results:", error);
            notification.error({
                message: 'Error',
                description: 'Failed to fetch lesson results',
            });
        }
    };

    const columns = React.useMemo<ColumnDef<typeof courses[number]>[]>(() => [
        { accessorKey: "id", header: () => "ID" },
        { accessorKey: "name", header: () => "Course Name" },
        { accessorKey: "intro", header: () => "Introduction" },
        { accessorKey: "diffLevel", header: () => "Difficulty Level" },
        { accessorKey: "recomLevel", header: () => "Recommended Level" },
        { accessorKey: "courseType", header: () => "Course Type" },
        { accessorKey: "speciField", header: () => "Specialized Field" },
    ], []);
    const formatDateTime = (dateTimeStr: string) => {
        const date = new Date(dateTimeStr);
        return date.toLocaleString('en-GB', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };
    useEffect(() => {
        const fetchData = async () => {
            try {
                // Lấy courseIds từ cả reviews và schedules
                const courseIdsFromReviews = reviews ? reviews.map(review => review.courseId) : [];
                const courseIdsFromSchedules = schedules ? schedules.map(schedule => schedule.courseId) : [];

                // Gộp và loại bỏ các courseId trùng lặp
                const allCourseIds = [...new Set([...courseIdsFromReviews, ...courseIdsFromSchedules])];

                if (allCourseIds.length > 0) {
                    const coursesPromises = allCourseIds.map(courseId =>
                        fetch(`${API_BASE_URL}/api/v1/courses/${courseId}`, {
                            method: 'GET',
                            headers: {
                                'Authorization': `Bearer ${token}`,
                                'Content-Type': 'application/json',
                            }
                        }).then(res => res.json())
                    );

                    const coursesResponses = await Promise.all(coursesPromises);

                    // Chuyển đổi mảng responses thành object với key là courseId
                    const coursesMap = coursesResponses.reduce((acc, response) => {
                        if (response.data) {
                            acc[response.data.id] = response.data;
                        }
                        return acc;
                    }, {});

                    setCourseDetails(coursesMap);

                }
            } catch (error) {
                console.error("Error fetching courses:", error);
            }
        };

        fetchData();
    }, [reviews, schedules, token]);

    const table = useReactTable({
        data: courses,
        columns,
        getCoreRowModel: getCoreRowModel(),
    });

    const fetchLessonsByCourseId = async (courseId: number) => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/v1/courses/${courseId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            const data = await response.json();
            const lessonIds = data.data.lessonIds;

            const lessonDetails = await Promise.all(
                lessonIds.map(async (id: number) => {
                    const lessonResponse = await fetch(`${API_BASE_URL}/api/v1/lessons/${id}`, {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        },
                    });
                    const lessonData = await lessonResponse.json();
                    return lessonData.data;
                })
            );

            setLessons(lessonDetails);
            setSelectedCourseId(courseId);
        } catch (error) {
            message.error("Don't have data to show!");
        }
    };

    const handleViewLessons = (courseId: number) => {
        fetchLessonsByCourseId(courseId);
    };

    return (
        <>
            <div className="bg-white shadow-md rounded-lg p-6">
                <div className="flex justify-between">
                    <Card title={"User Information"} style={{ height: 300, width: 280 }}>
                        <div className="mb-4"><strong>ID:</strong> {user.id}</div>
                        <div className="mb-4"><strong>Name:</strong> {user.name}</div>
                        <div className="mb-4"><strong>Email:</strong> {user.email}</div>
                        <div className="mb-4"><strong>Specialized Field:</strong> {user.speciField}</div>
                        <div className="mb-4"><strong>English Level:</strong> {user.englishlevel}</div>
                    </Card>
                    <Card title={"Schedule"} style={{ height: 300, width: 250 }}>
                        <div className="space-y-2 overflow-auto max-h-[220px] scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100">
                            {schedules.map((schedule) => (
                                <div key={schedule.id} className="p-3 bg-gray-50 rounded">
                                    {/* Course Name */}


                                    {/* Schedule Type & Time */}
                                    <div className="text-gray-600">
                                        <div className="font-semibold">
                                            {schedule.isDaily ? "Daily" : "Weekly"}
                                        </div>
                                        <div className="text-sm">
                                            {formatDateTime(schedule.scheduleTime)}
                                        </div>
                                    </div>
                                </div>
                            )
                            )}
                        </div>
                    </Card>
                    <Card title={"Reviews"} style={{ height: 300, width: 250 }}>
                        <div className="space-y-4 overflow-auto max-h-[220px]">
                            {reviews.length === 0 && (
                                <div className="text-center text-gray-500">No reviews available</div>
                            )}
                            {reviews.map((review) => (
                                <div key={review.id} className="border-b pb-3">
                                    {/* Course Name */}
                                    <div className="text-sm font-medium text-gray-600 mb-2">
                                        {courseDetails[review.courseId]?.name || 'Loading...'}
                                    </div>

                                    {/* Rating and Likes */}
                                    <div className="flex items-center justify-between mb-2">
                                        <span className="text-yellow-500">
                                            {Array(review.numStar).fill('★').join('')}
                                            {Array(5 - review.numStar).fill('☆').join('')}
                                        </span>
                                        <div className="flex items-center gap-1">
                                            <span>❤️</span>
                                            <span className="text-sm">{review.numLike}</span>
                                        </div>
                                    </div>

                                    {/* Review Content */}
                                    <p className="mt-1 text-sm text-gray-600">
                                        {review.reContent}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </Card>
                </div>

                <h3 className="text-xl font-bold mt-6 mb-4">Courses Completed</h3>
                <table className="min-w-full border-collapse border border-gray-200">
                    <thead>
                        {table.getHeaderGroups().map(headerGroup => (
                            <tr key={headerGroup.id}>
                                {headerGroup.headers.map(header => (
                                    <th key={header.id} className="border border-gray-200 p-2 text-left">
                                        {flexRender(header.column.columnDef.header, header.getContext())}
                                    </th>
                                ))}
                                <th key="actions" className="border border-gray-200 p-2 text-left"></th>
                            </tr>
                        ))}
                    </thead>
                    <tbody>
                        {table.getRowModel().rows.map(row => (
                            <tr key={row.id}>
                                {row.getVisibleCells().map(cell => (
                                    <td key={cell.id} className="border border-gray-200 p-2">{cell.getValue() as React.ReactNode}</td>
                                ))}
                                <td key={row.id} className="border border-gray-200 p-2 text-left">
                                    <Button type="link" onClick={() => handleViewLessons(row.original.id)}>View</Button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>

                {selectedCourseId && (
                    <div className="mt-6">
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="text-xl font-bold">List Lessons</h3>
                            <Button type="link" onClick={() => setSelectedCourseId(null)}>Hide</Button>
                        </div>
                        <List
                            grid={{ gutter: 16, column: 2 }}
                            dataSource={lessons}
                            renderItem={lesson => (
                                <List.Item>
                                    <Card
                                        title={<span className="font-semibold text-lg">{lesson.name}</span>}
                                        className="hover:shadow-lg transition-shadow duration-300"
                                        style={{
                                            height: 'auto',
                                            minHeight: 150,
                                            borderRadius: '12px',
                                            overflow: 'hidden',
                                            border: '1px solid #e5e7eb',
                                        }}
                                        headStyle={{
                                            background: '#f9fafb',
                                            borderBottom: '1px solid #e5e7eb',
                                            borderTopLeftRadius: '12px',
                                            borderTopRightRadius: '12px',
                                            padding: '12px 16px',
                                        }}
                                        bodyStyle={{
                                            padding: '16px',
                                        }}
                                    >
                                        <div className="flex flex-col h-full">
                                            {/* Skill Type */}
                                            <div className="mb-3">
                                                <span className="inline-block px-3 py-1 text-sm rounded-full bg-blue-50 text-blue-600">
                                                    {lesson.skillType}
                                                </span>
                                            </div>



                                            {/* View Results Button */}
                                            <div className="mt-auto">
                                                <Button
                                                    type="primary"
                                                    onClick={() => handleViewResults(lesson)}
                                                    className="w-50"
                                                    style={{
                                                        backgroundColor: '#2563eb',
                                                        borderColor: '#2563eb',
                                                        color: '#fff',
                                                    }}
                                                >
                                                    View Results
                                                </Button>
                                            </div>
                                        </div>
                                    </Card>
                                </List.Item>
                            )}
                        />
                    </div>
                )}
            </div>
            <ResultModal
                isOpen={isResultModalOpen}
                onClose={() => setIsResultModalOpen(false)}
                results={selectedLessonResults}
            />
        </>
    );
};

export default UserProfile;
