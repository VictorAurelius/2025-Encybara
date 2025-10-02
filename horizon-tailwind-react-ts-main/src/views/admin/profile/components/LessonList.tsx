import { EditOutlined, DeleteOutlined, InfoCircleOutlined } from "@ant-design/icons";
import { useAuth } from "hooks/useAuth";
import React, { useEffect, useRef, useState } from "react";
import { Button, message, Popconfirm, Tooltip } from "antd";
import ModuleLesson from "./module.lesson";
import { notification } from "antd";
import lessonService from "../../../../service/lesson.service";
import { useCache } from "../../../../hooks/useCache";

export interface Lesson {
    id: number;
    name: string;
    skillType: string;
    questionIds: number[];
}

interface LessonListProps {
    lessons: Lesson[];
    courseId: number;
    fetchLessons: () => void;
}

const LessonList: React.FC<LessonListProps> = ({ lessons, courseId, fetchLessons }) => {
    const { token } = useAuth();
    const { getCacheStats } = useCache();
    const [openModal, setOpenModal] = useState(false);
    const [selectedLessons, setSelectedLessons] = useState<number[]>([]);
    const [selectedLesson, setSelectedLesson] = useState<Lesson | null>(null);
    useEffect(() => {
        // Fetch lessons in the course and set them as selected
        const fetchCourseLessons = async () => {
            try {
                console.log(`📋 Fetching course ${courseId} lesson details...`);
                const courseDetails = await lessonService.getCourseDetails(courseId);
                console.log("courseLessonIds:", courseDetails);
                setSelectedLessons(courseDetails.lessonIds || []);
            } catch (error) {
                console.error("Error fetching course lessons:", error);
                message.error("Failed to fetch course lessons");
            }
        };

        fetchCourseLessons();
    }, [courseId]);
    const toggleSelectLesson = async (lessonId: number) => {
        const isSelected = selectedLessons.includes(lessonId);

        try {
            if (isSelected) {
                console.log(`⟖ Removing lesson ${lessonId} from course ${courseId}`);
                await lessonService.removeLessonFromCourse(courseId, lessonId);
                setSelectedLessons(prevSelected => prevSelected.filter(id => id !== lessonId));
                message.success("Lesson removed successfully");
            } else {
                console.log(`➕ Adding lesson ${lessonId} to course ${courseId}`);
                await lessonService.addLessonsToCourse(courseId, [lessonId]);
                setSelectedLessons(prevSelected => [...prevSelected, lessonId]);
                notification.success({
                    message: "Add lesson successfully",
                    placement: "topLeft",
                });
            }
        } catch (error) {
            console.error("Error toggling lesson:", error);
            notification.error({
                message: "Error",
                description: isSelected ? "Failed to remove lesson" : "Failed to add lesson",
                placement: "topRight",
            });
        }
    };

    const handleEditLesson = (lesson: Lesson) => {
        setSelectedLesson(lesson);
        setOpenModal(true);
    };
    const handleDeleteLesson = async (lessonId: number) => {
        try {
            console.log(`🗑️ Deleting lesson ${lessonId}`);
            await lessonService.deleteLesson(lessonId);
            message.success("Lesson deleted successfully");
            fetchLessons();
        } catch (error) {
            console.error("Error deleting lesson:", error);
            message.error("Failed to delete lesson");
        }
    };

    return (
        <div className="bg-white">
            <div className="flex justify-between items-center mb-6">
                <h3 className="text-lg font-bold text-gray-800">Lesson List</h3>
                <div className="flex gap-2">    
                    <Button type="primary" className="hover:opacity-90" onClick={() => setOpenModal(true)}>
                        Add Lesson
                    </Button>
                </div>
            </div>

            <div className="mt-8">
                <div className="max-h-[250px] overflow-y-auto">
                    <table className="w-full">
                        <thead className="sticky top-0 bg-gray-50 z-10">
                            <tr className="!border-px !border-gray-400">
                                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Choose</th>
                                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">ID</th>
                                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Name</th>
                                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Skill Type</th>
                                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {lessons.map((lesson) => (
                                <tr key={lesson.id} className="hover:bg-gray-50">
                                    <td className="border-b border-gray-200 py-3 pr-4">
                                        <input
                                            type="checkbox"
                                            checked={selectedLessons.includes(lesson.id)}
                                            onChange={() => toggleSelectLesson(lesson.id)}
                                        />
                                    </td>
                                    <td className="border-b border-gray-200 py-3 pr-4">{lesson.id}</td>
                                    <td className="border-b border-gray-200 py-3 pr-4">{lesson.name}</td>
                                    <td className="border-b border-gray-200 py-3 pr-4">{lesson.skillType}</td>
                                    <td className="border-b border-gray-200 py-3 pr-4">
                                        <EditOutlined
                                            style={{
                                                fontSize: 20,
                                                color: '#ffa500',
                                            }}
                                            type=""
                                            onClick={() => {
                                                handleEditLesson(lesson)
                                            }} />
                                        <Popconfirm
                                            placement="leftTop"
                                            title={"Confirm delete lesson"}
                                            description={"Are you sure you want to delete this lesson ?"}
                                            onConfirm={() => handleDeleteLesson(lesson.id)}
                                            okText="Ok"
                                            cancelText="Cancel"
                                        >
                                            <span style={{ cursor: "pointer", margin: "0 10px" }}>
                                                <DeleteOutlined
                                                    style={{
                                                        fontSize: 20,
                                                        color: '#ff4d4f',
                                                    }} />
                                            </span>
                                        </Popconfirm>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {openModal && (
                <ModuleLesson
                    openModal={openModal}
                    setOpenModal={setOpenModal}
                    reloadTable={fetchLessons}
                    listLesson={selectedLesson}
                    setListLesson={setSelectedLesson}
                />
            )}
        </div>
    );
};

export default LessonList;