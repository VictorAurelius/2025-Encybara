
import { FooterToolbar, ModalForm, ProCard, ProFormSelect, ProFormSwitch, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { Col, Form, Row, message, notification, Input, Button, Switch, Upload } from "antd";
import { API_BASE_URL } from "service/api.config";
import { useEffect, useState } from "react";
import { CheckCircleOutlined, DeleteOutlined, InboxOutlined, PlusOutlined, VideoCameraOutlined } from "@ant-design/icons";

export interface IQuestion {
    id?: number;
    quesContent: string;
    keyword: string;
    quesType: string;
    skillType: string;
    point: number;
    quesMaterial: string;
    questionChoices: IQuestionChoice[];
}
interface IQuestionChoice {
    id?: number;
    choiceContent: string;
    choiceKey: boolean;
}
interface IProps {
    openModal: boolean;
    setOpenModal: (v: boolean) => void;
    reloadTable: () => void;
    singleQuestion: IQuestion | null;
    setSingleQuestion: (v: any) => void;
}
const ModalQuestion = (props: IProps) => {
    const { openModal, setOpenModal, reloadTable, singleQuestion, setSingleQuestion } = props;
    const [form] = Form.useForm();
    const [questionType, setQuestionType] = useState(singleQuestion?.quesType || '');
    const [questionMaterial, setQuestionMaterial] = useState(singleQuestion?.quesMaterial || '');
    const handleQuestionTypeChange = (value: string) => {
        setQuestionType(value);
        // Reset questionChoices nếu chuyển sang LISTENING
        if (value === 'LISTENING') {
            form.setFieldValue('questionChoices', []);
        }
    };
    useEffect(() => {
        if (singleQuestion) {
            form.setFieldsValue({
                quesContent: singleQuestion.quesContent,
                quesType: singleQuestion.quesType,
                skillType: singleQuestion.skillType,
                point: singleQuestion.point,
                questionChoices: singleQuestion.questionChoices,
                keyword: singleQuestion.keyword,
                quesMaterial: singleQuestion.quesMaterial
            });
            setQuestionType(singleQuestion.quesType);
        }
    }, [singleQuestion, form]);

    const submitQuestion = async (valuesForm: any) => {
        const { quesContent, keyword, quesType, skillType, point, quesMaterial } = valuesForm;
        const questionChoices = quesType === 'LISTENING' ? [] : valuesForm.questionChoices;
        // Xử lý các loại câu hỏi khác như cũ
        const question = {
            id: singleQuestion?.id,
            quesContent,
            keyword,
            quesType,
            skillType,
            point,
            questionChoices,
            quesMaterial
        };

        console.log("question:", question);

        const res = await fetch(`${API_BASE_URL}/api/v1/questions`, {
            method: singleQuestion?.id ? "PUT" : "POST",
            body: JSON.stringify(question),
            headers: {
                'Content-Type': 'application/json',
            },
        });

        const data = await res.json();
        if (res.ok) {
            message.success(singleQuestion?.id ? "Update question successfully" : "Create question successfully");
            handleReset();
            reloadTable();
        } else {
            notification.error({
                message: 'An error occurred',
                description: data.message
            });
        }
    };

    const handleReset = async () => {
        form.resetFields();
        setOpenModal(false);
        setSingleQuestion(null);
    }

    return (
        <>
            <ModalForm
                title={<>{singleQuestion?.id ? "Update Question" : "Create Question"}</>}
                open={openModal}
                modalProps={{
                    onCancel: () => { handleReset() },
                    afterClose: () => handleReset(),
                    destroyOnClose: true,
                    width: 900,
                    keyboard: false,
                    maskClosable: false,
                    okText: <>{singleQuestion?.id ? "Update" : "Create"}</>,
                    cancelText: "Cancel"

                }}
                scrollToFirstError={true}
                preserve={false}
                form={form}
                onFinish={submitQuestion}

            >
                <Row gutter={16}>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormTextArea
                            label="Question Content"
                            name="quesContent"
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Enter question content"
                            fieldProps={{
                                autoSize: { minRows: 3, maxRows: 6 }, // Tự động điều chỉnh chiều cao
                            }}
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="Keyword"
                            name="keyword"
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Enter keyword"
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormSelect
                            label="Question Type"
                            name="quesType"
                            valueEnum={{
                                MULTIPLE: 'MULTIPLE',
                                CHOICE: 'CHOICE',
                                TEXT: 'TEXT',
                                LISTENING: 'LISTENING'
                            }}
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Enter question type"
                            fieldProps={{
                                onChange: handleQuestionTypeChange
                            }}
                        />
                    </Col>

                    <Col span={24}>
                        <ProFormSelect
                            label="Skill Type"
                            name="skillType"
                            valueEnum={{
                                READING: 'READING',
                                LISTENING: 'LISTENING',
                                SPEAKING: 'SPEAKING',
                                WRITING: 'WRITING'
                            }}
                            rules={[{ required: true, message: 'Please do not leave blank' }]}
                            placeholder="Enter skill type"
                        />
                    </Col>
                    <Col span={24}>
                        {questionType == 'LISTENING' ? (
                            <div>No choices for listening question</div>

                        ) : (
                            <ProCard
                                title="Question Choices"
                                subTitle="The choices allowed for this question"
                                headStyle={{ color: '#d81921' }}
                                style={{ marginBottom: 20 }}
                                headerBordered
                                size="small"
                                bordered
                            >
                                <Form.List
                                    name="questionChoices"
                                    rules={[
                                        {
                                            validator: async (_, questionChoices) => {
                                                if (!questionChoices || questionChoices.length < 1) {
                                                    return Promise.reject(new Error('At least one choice is required'));
                                                }
                                            },
                                        },
                                    ]}
                                >
                                    {(fields, { add, remove }, { errors }) => (
                                        <div className="space-y-3">
                                            {fields.map((field, index) => (
                                                <div key={field.key} className="flex items-center gap-4 p-3 border rounded-lg">
                                                    <Form.Item
                                                        {...field}
                                                        name={[field.name, 'choiceContent']}
                                                        rules={[{ required: true, message: 'Please input choice content' }]}
                                                        style={{ flex: 1, marginBottom: 0 }}
                                                    >
                                                        <Input
                                                            placeholder={`Enter choice ${String.fromCharCode(65 + index)}`}
                                                        />
                                                    </Form.Item>

                                                    <Form.Item
                                                        {...field}
                                                        name={[field.name, 'choiceKey']}
                                                        valuePropName="checked"
                                                        style={{ marginBottom: 0 }}
                                                    >
                                                        <Switch
                                                            checkedChildren="Correct"
                                                            unCheckedChildren="Incorrect"
                                                            onChange={(checked) => {
                                                                const newQuestionChoices = [...form.getFieldValue('questionChoices')];
                                                                newQuestionChoices[index] = {
                                                                    ...newQuestionChoices[index],
                                                                    choiceKey: checked
                                                                };
                                                                console.log("newQuestionChoices:", newQuestionChoices);
                                                                form.setFieldsValue({ questionChoices: newQuestionChoices });
                                                                console.log("questionChoices sau khi set:", form.getFieldValue('questionChoices'));
                                                            }}
                                                        />
                                                    </Form.Item>

                                                    {fields.length > 1 && (
                                                        <Button
                                                            type="text"
                                                            danger
                                                            onClick={() => remove(field.name)}
                                                            icon={<DeleteOutlined />}
                                                        />
                                                    )}
                                                </div>
                                            ))}

                                            <Button
                                                type="dashed"
                                                onClick={() => add({ choiceContent: '', choiceKey: false })}
                                                block
                                                icon={<PlusOutlined />}
                                            >
                                                Add Choice
                                            </Button>

                                            <Form.ErrorList errors={errors} />
                                        </div>
                                    )}
                                </Form.List>
                            </ProCard>
                        )}
                    </Col>
                    <Col span={24}>
                        <ProFormSelect
                            label="Point"
                            name="point"
                            valueEnum={{
                                10: '10'
                            }}
                            rules={[
                                { required: true, message: 'Please do not leave blank' },
                            ]}
                            placeholder="Enter point"
                            disabled={!!singleQuestion?.id}
                            fieldProps={{
                                className: singleQuestion?.id ? 'bg-gray-50' : ''
                            }}
                        />
                    </Col>
                </Row>
            </ModalForm >
        </>
    )
}
export default ModalQuestion;