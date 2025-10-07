import { Lesson } from "./LessonList";
import { FooterToolbar, ModalForm, ProCard, ProFormSelect, ProFormSwitch, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { Col, Form, Row, message, notification } from "antd";
import { useEffect, useState } from "react";
import { CheckSquareOutlined } from "@ant-design/icons";
import { useAuth } from "hooks/useAuth";
import lessonService from "../../../../service/lesson.service";

interface IProps {
    openModal: boolean;
    setOpenModal: (v: boolean) => void;
    reloadTable: () => void;
    listLesson: Lesson | null;
    setListLesson: (v: Lesson) => void;
}

const ModuleLesson = (props: IProps) => {
    const { openModal, setOpenModal, reloadTable, listLesson, setListLesson } = props;
    const [form] = Form.useForm();
    const { token } = useAuth();
    const [questions, setQuestions] = useState([]);
    const [selectedQuestionIds, setSelectedQuestionIds] = useState<number[]>([]);

    useEffect(() => {
        if (listLesson) {
            form.setFieldsValue({
                name: listLesson.name,
                skillType: listLesson.skillType,
                questionIds: listLesson.questionIds,
            });
            setSelectedQuestionIds(listLesson.questionIds || []);
        }
    }, [listLesson, form]);

    useEffect(() => {
        const fetchQuestions = async () => {
            if (!listLesson?.skillType) return;
            
            try {
                const questionsData = await lessonService.getQuestionsBySkillType(listLesson.skillType);
                const formattedQuestions = questionsData.map((q: any) => ({
                    label: q.quesContent,
                    value: q.id,
                }));
                
                setQuestions(formattedQuestions);
            } catch (error) {
                console.error("Error fetching questions:", error);
                message.error("Failed to fetch questions");
            }
        };

        fetchQuestions();
    }, [listLesson?.skillType]);

    const handleReset = async () => {
        form.resetFields();
        setOpenModal(false);
        setListLesson(null);
    }
    const handleQuestionChange = async (newSelectedIds: number[]) => {
        newSelectedIds = newSelectedIds || [];
        const addedIds = newSelectedIds.filter(id => !selectedQuestionIds.includes(id));
        const removedIds = selectedQuestionIds.filter(id => !newSelectedIds.includes(id));

        if (!listLesson?.id) return;

        try {
            if (addedIds.length > 0) {
                await lessonService.addQuestionsToLesson(listLesson.id, addedIds);
                message.success("Questions added successfully");
            }

            if (removedIds.length > 0) {
                for (const questionId of removedIds) {
                    await lessonService.removeQuestionFromLesson(listLesson.id, questionId);
                }
                message.success(`Questions removed successfully`);
            }

            setSelectedQuestionIds(newSelectedIds);
        } catch (error) {
            console.error("Error updating questions:", error);
            message.error("Failed to update questions");
        }
    };
    const submitLesson = async (values: any) => {
        const { name, skillType } = values;
        const lesson = { name, skillType };
        
        try {
            if (listLesson?.id) {
                await lessonService.updateLesson(listLesson.id, lesson);
                message.success("Lesson updated successfully");
            } else {
                await lessonService.createLesson(lesson);
                message.success("Lesson created successfully");
            }
            
            handleReset();
            reloadTable();
        } catch (error) {
            console.error("Error submitting lesson:", error);
            notification.error({
                message: 'An error occurred',
                description: 'Failed to save lesson'
            });
        }
    };
    return (
        <>
            <ModalForm
                title={<>{listLesson?.id ? "Update Lesson" : "Create Lesson"}</>}
                open={openModal}
                modalProps={{
                    onCancel: () => { handleReset() },
                    afterClose: () => handleReset(),
                    destroyOnClose: true,
                    width: 900,
                    keyboard: false,
                    maskClosable: false,
                    okText: <>{listLesson?.id ? "Update" : "Create"}</>,
                    cancelText: "Cancel"

                }}
                scrollToFirstError={true}
                preserve={false}
                form={form}
                onFinish={submitLesson}

            >
                <Row gutter={16}>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="Name Lesson"
                            name="name"
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Enter name"
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormSelect
                            label="Skill Type"
                            name="skillType"
                            valueEnum={
                                {
                                    LISTENING: `LISTENING`,
                                    READING: `READING`,
                                    WRITING: `WRITING`,
                                    SPEAKING: `SPEAKING`,
                                }
                            }
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Enter skillType"
                        />
                    </Col>

                    <Col lg={12} md={12} sm={24} xs={24}>
                        {listLesson?.id && (
                            <ProFormSelect
                                label="List question"
                                name="questionIds"
                                options={questions}
                                mode="multiple"

                                placeholder="Select question"
                                onChange={handleQuestionChange}

                            />
                        )}
                    </Col>
                </Row>
            </ModalForm>
        </>
    )
}
export default ModuleLesson;