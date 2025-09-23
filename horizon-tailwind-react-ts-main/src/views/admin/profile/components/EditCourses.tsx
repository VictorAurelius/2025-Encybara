import React, { useState, useEffect } from "react";
import { useAuth } from "hooks/useAuth";
import { ModalForm, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { Row, Col, message } from "antd";
import { API_BASE_URL } from "service/api.config";

interface EditCourseProps {
    courseId: number;
    onClose: () => void;
    onSuccess: () => void;
}

const EditCourse: React.FC<EditCourseProps> = ({ courseId, onClose, onSuccess }) => {
    const { token } = useAuth();
    const [courseData, setCourseData] = useState<any>({
        name: "",
        intro: "",
        diffLevel: 0,
        recomLevel: 0,
        courseType: "",
        speciField: "",
        group: "",
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchCourse = async () => {
            try {
                if (courseId) {
                    const response = await fetch(`${API_BASE_URL}/api/v1/courses/${courseId}`, {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                        },
                    });
                    if (!response.ok) {
                        throw new Error("Failed to fetch course data");
                    }
                    const data = await response.json();
                    setCourseData(data.data);
                }
                else {
                    setCourseData({
                        name: "",
                        intro: "",
                        diffLevel: 0,
                        recomLevel: 0,
                        courseType: "",
                        speciField: "",
                        group: ""
                    })

                }
            } catch (error) {
                console.error("Error fetching course data:", error);
                setError("Failed to fetch course data");
            } finally {
                setLoading(false);
            }
        };

        fetchCourse();
    }, [courseId, token]);

    const handleSubmit = async (values: any) => {
        try {
            if (courseId) {
                const response = await fetch(`${API_BASE_URL}/api/v1/courses/${courseId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`,
                    },
                    body: JSON.stringify(values),
                });
                if (!response.ok) {
                    throw new Error("Failed to update course");
                }

                message.success("Course updated successfully!");
                onSuccess();
                onClose();
            } else {
                const response = await fetch(`${API_BASE_URL}/api/v1/courses`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`,
                    },
                    body: JSON.stringify(values),
                });
                if (!response.ok) {
                    throw new Error("Failed to update course");
                }

                message.success("Course updated successfully!");
                onSuccess();
                onClose();
            }
        } catch (error) {
            console.error("Error updating course:", error);
            setError("Failed to update course");
        }
    };

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <ModalForm
            title={<>{courseId ? "Update course" : "Create course"}</>}
            visible={true}
            onFinish={handleSubmit}
            initialValues={courseData}
            modalProps={{
                onCancel: onClose,
                destroyOnClose: true,
                okText: <>{courseId ? "Update" : "Create"}</>,
                cancelText: "Cancel"
            }}
        >
            <Row gutter={16}>
                <Col span={12}>
                    <ProFormTextArea
                        name="name"
                        label="Course name"
                        placeholder="Enter course name"
                        rules={[{ required: true, message: 'Please enter course name' }]}
                    />
                </Col>
                <Col span={12}>
                    <ProFormTextArea
                        name="intro"
                        label="Introduction"
                        placeholder="Enter introduction"
                        rules={[{ required: true, message: 'Please enter introduction' }]}
                    />
                </Col>
                <Col span={12}>
                    <ProFormText
                        name="diffLevel"
                        label="Diff Level"
                        placeholder="Enter diff level"
                        rules={[{ required: true, message: 'Please enter diff level' }]}
                    />
                </Col>
                <Col span={12}>
                    <ProFormText
                        name="recomLevel"
                        label="Recom Level"
                        placeholder="Enter recom level"
                        rules={[{ required: true, message: 'Please enter recom level' }]}
                    />
                </Col>
                <Col span={12}>
                    <ProFormSelect
                        name="courseType"
                        label="Course type"
                        valueEnum={{
                            ALLSKILLS: 'ALLSKILLS',
                            READING: 'READING',
                            LISTENING: 'LISTENING',
                            SPEAKING: 'SPEAKING',
                            WRITING: 'WRITING',
                        }}
                        placeholder="Enter course type"
                        rules={[{ required: true, message: 'Please enter course type' }]}
                    />
                </Col>
                <Col span={12}>
                    <ProFormSelect
                        name="speciField"
                        label="Special Field"
                        valueEnum={{
                            CONSTRUCTION: 'CONSTRUCTION',
                            IT: 'IT',
                            ELECTRICITY: 'ELECTRICITY',
                            ECONOMIC: 'ECONOMIC',
                        }}
                        placeholder="Enter special field"
                        rules={[{ required: true, message: 'Please enter special field' }]}
                    />
                </Col>
                <Col span={12}>
                    <ProFormText
                        name="group"
                        label="Group "
                        placeholder="Enter group"
                        rules={[{ required: true, message: 'Please enter group' }]}
                    />
                </Col>
            </Row>
        </ModalForm>
    );
};

export default EditCourse;