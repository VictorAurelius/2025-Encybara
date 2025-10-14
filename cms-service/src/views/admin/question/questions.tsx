import DataTable from "../permission/components/data.table";
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined, UploadOutlined, InfoCircleOutlined, EyeOutlined } from "@ant-design/icons";
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Space, Dropdown, Input, Tooltip } from "antd";
import { useState, useRef, useEffect } from 'react';
import ModalQuestion from "./components/module.question";
import { IQuestion } from "./components/module.question";
import ModalUpload from "./components/module.upload";
import ModalSampleAnswer from "./components/modal.sample-answer";
import { App } from 'antd';
import Access from "../access";
import questionService from "../../../service/question.service";
import { useCache } from "../../../hooks/useCache";
import { API_BASE_URL } from "service/api.config";


const QuestionPage = () => {
    const { getCacheStats } = useCache();
    const [openModal, setOpenModal] = useState<boolean>(false);
    const [openModalUpload, setOpenModalUpload] = useState<boolean>(false);
    const [openModalSampleAnswer, setOpenModalSampleAnswer] = useState<boolean>(false);
    const [dataInit, setDataInit] = useState<IQuestion | null>(null);
    const [dataSource, setDataSource] = useState<IQuestion[]>([]); // Khởi tạo state cho dataSource
    const [loading, setLoading] = useState<boolean>(true); // Khởi tạo state cho loading
    const [pageSize, setPageSize] = useState<number>(10); // Kích thước trang
    const [currentPage, setCurrentPage] = useState<number>(1); // Trang hiện tại
    const [total, setTotal] = useState<number>(0); // Thêm state cho tổng số lượng bản ghi
    const [uploadData, setUploadData] = useState<any>(null); // State để lưu dữ liệu upload
    const [lessonMap, setLessonMap] = useState<{ [key: number]: Array<{ id: number, name: string }> }>({});
    const [selectedFilters, setSelectedFilters] = useState({
        quesType: undefined,
        keyword: undefined,
        skillType: undefined
    });
    const { message, notification } = App.useApp();
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
    const [lessonList, setLessonList] = useState<Array<{ id: number; name: string }>>([]);
    const [quesID, setQuesID] = useState<number>(0);

    const reloadTable = async () => {
        setLoading(true);
        try {

            // Fetch questions with filters and pagination
            const questionsData = await questionService.getQuestions(
                {
                    quesType: selectedFilters.quesType,
                    skillType: selectedFilters.skillType,
                    keyword: selectedFilters.keyword,
                },
                { page: currentPage, size: pageSize }
            );

            // Fetch question-lesson mapping
            const questionLessonMap = await questionService.getQuestionLessonMap();

            setLessonMap(questionLessonMap);
            setDataSource(questionsData.content);
            setTotal(questionsData.totalPages * pageSize);
        } catch (error) {
            console.error("Error fetching data:", error);
            message.error("Failed to fetch questions");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        reloadTable(); // Gọi lại khi component mount hoặc khi currentPage/pageSize thay đổi
    }, [currentPage, pageSize, selectedFilters]);

    useEffect(() => {
        const fetchLessons = async () => {
            try {
                const lessons = await questionService.getAllLessons();
                setLessonList(lessons);
            } catch (error) {
                console.error("Error fetching lessons:", error);
                message.error("Failed to fetch lessons");
            }
        };
        fetchLessons();
    }, []);

    // Thêm useEffect để log dataSource

    const tableRef = useRef<ActionType>(null);

    const handleDeleteQuestion = async (id: number | undefined) => {
        if (!id) return;

        try {
            await questionService.deleteQuestion(id);
            message.success('Question deleted successfully');
            reloadTable();
        } catch (error) {
            console.error('Error deleting question:', error);
            message.error('Failed to delete question');
        }
    };

    const fetchUploadData = async (questionId: number) => {
        try {
            // This would need to be implemented in questionService if needed
            // For now keeping the direct API call as it's specific to material management
            const res = await fetch(`${API_BASE_URL}/api/v1/material/questions/${questionId}`,
                {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('admin_token')}`,
                        'Content-Type': 'application/json',
                    },
                });
            if (res.ok) {
                const data = await res.json();
                setUploadData(data);
                setQuesID(questionId);
                setOpenModalUpload(true);
            }
        } catch (error) {
            console.error('Error fetching upload data:', error);
        }
    };

    const handleDeleteSelected = async () => {
        if (selectedRowKeys.length === 0) {
            message.warning('Please select questions to delete');
            return;
        }

        try {
            const questionIds = selectedRowKeys.map(key => Number(key));
            await questionService.deleteMultipleQuestions(questionIds);
            message.success(`Deleted ${questionIds.length} questions successfully`);
            setSelectedRowKeys([]);
            reloadTable();
        } catch (error) {
            console.error('Error deleting selected questions:', error);
            message.error('Failed to delete selected questions');
        }
    };


    const handleQuestionLesson = async (lessonId: number, action: 'add' | 'remove', questionId?: number) => {
        try {
            if (action === 'add') {
                const questionIds = selectedRowKeys.map(key => Number(key));
                console.log("questionIds:", questionIds);
                if (questionIds.length === 0) {
                    message.warning('Please select questions first');
                    return;
                }
                const res = await fetch(`${API_BASE_URL}/api/v1/lessons/${lessonId}/questions`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ questionIds })
                });

                if (res.ok) {
                    message.success('Added questions to lesson successfully');
                    reloadTable();
                }
            } else if (questionId) {
                const res = await fetch(`${API_BASE_URL}/api/v1/lessons/${lessonId}/questions`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ questionId })
                });

                if (res.ok) {
                    message.success('Success delete question from lesson');
                    reloadTable();
                }
            }
        } catch (error) {
            message.error('Failed to update lesson');
        }
    };

    const columns: ProColumns<IQuestion>[] = [
        {
            title: 'Id',
            dataIndex: 'id',
            width: 50,
            render: (text, record, index, action) => {
                return (
                    <a href="#" onClick={() => {
                        setDataInit(record);
                    }}>
                        {record.id}
                    </a>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Question Content',
            dataIndex: 'quesContent',
            sorter: false,
        },
        {
            title: 'Question Type',
            dataIndex: 'quesType',
            filters: [
                { text: 'LISTENING', value: 'LISTENING' },
                { text: 'MULTIPLE', value: 'MULTIPLE' },
                { text: 'TEXT', value: 'TEXT' },
                { text: 'CHOICE', value: 'CHOICE' },
                { text: 'WRITING', value: 'WRITING' },
                { text: 'SPEAKING', value: 'SPEAKING' },
            ],
            filterMode: 'menu',
            filtered: true,
            onFilter: true,
            onFilterDropdownOpenChange: (visible) => {
                if (!visible) {
                    const selectedItem = document.querySelector('.ant-dropdown-menu-item-selected');
                    const filterValue = selectedItem?.getAttribute('data-menu-id')?.split('-').pop();
                    console.log("filterValue:", filterValue);
                    setSelectedFilters({
                        ...selectedFilters,
                        quesType: filterValue || undefined
                    });
                }
            }
        },
        {
            title: 'Skill Type',
            dataIndex: 'skillType',
            filters: [
                { text: 'READING', value: 'READING' },
                { text: 'LISTENING', value: 'LISTENING' },
                { text: 'WRITING', value: 'WRITING' },
                { text: 'SPEAKING', value: 'SPEAKING' },
            ],
            filterMode: 'menu',
            filtered: true,
            onFilter: true,
            onFilterDropdownOpenChange: (visible) => {
                if (!visible) {
                    const selectedItem = document.querySelector('.ant-dropdown-menu-item-selected');
                    const filterValue = selectedItem?.getAttribute('data-menu-id')?.split('-').pop();
                    console.log("filterValue:", filterValue);
                    setSelectedFilters({
                        ...selectedFilters,
                        quesType: filterValue || undefined
                    });
                }
            }
        },
        {
            title: 'Point',
            dataIndex: 'point',
            sorter: false,
        },
        {
            title: 'Lessons',
            dataIndex: 'id',
            width: 150,
            render: (questionId, record) => {
                const assignedLessons = lessonMap[record.id] || [];
                return (
                    <Dropdown
                        menu={{
                            items: assignedLessons.map(lesson => ({
                                key: lesson.id,
                                label: (
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span>{lesson.name}</span>
                                        <DeleteOutlined
                                            style={{ color: '#ff4d4f', cursor: 'pointer' }}
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleQuestionLesson(lesson.id, 'remove', record.id);
                                            }}
                                        />
                                    </div>
                                )
                            }))
                        }}
                        trigger={['click']}
                    >
                        <Button style={{ width: '100%' }}>
                            {assignedLessons.length > 0
                                ? `${assignedLessons.length} Lesson${assignedLessons.length > 1 ? 's' : ''}`
                                : 'No lessons'
                            }
                        </Button>
                    </Dropdown>
                );
            },

        },

        {
            title: 'Actions',
            hideInSearch: true,
            width: 80,
            render: (_value, entity, _index, _action) => (
                <Space>
                    <EditOutlined
                        style={{
                            fontSize: 20,
                            color: '#ffa500'
                        }}

                        onClick={() => {
                            setOpenModal(true);
                            setDataInit(entity);

                        }} />
                    <Popconfirm
                        placement="leftTop"
                        title={"Confirm delete question"}
                        description={"Are you sure you want to delete this question ?"}
                        onConfirm={() => handleDeleteQuestion(entity.id)}
                        okText="Confirm"
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
                    <UploadOutlined
                        style={{
                            fontSize: 20,
                            color: '#ffa500',
                            cursor: 'pointer',
                        }}
                        onClick={async () => {
                            setQuesID(entity.id);
                            setOpenModalUpload(true);
                            await fetchUploadData(entity.id); // Lấy dữ liệu trước khi mở modal

                        }} />
                    <Tooltip title={entity.quesType === 'SPEAKING' ? 'View Sample Answers' : 'Only available for SPEAKING questions'}>
                        <EyeOutlined
                            style={{
                                fontSize: 20,
                                color: entity.quesType === 'SPEAKING' ? '#1890ff' : '#d9d9d9',
                                cursor: entity.quesType === 'SPEAKING' ? 'pointer' : 'not-allowed',
                            }}
                            onClick={() => {
                                if (entity.quesType === 'SPEAKING') {
                                    setQuesID(entity.id);
                                    setDataInit(entity);
                                    setOpenModalSampleAnswer(true);
                                }
                            }} />
                    </Tooltip>
                </Space>
            ),
        },
    ];
    return (
        <div>
            <DataTable<IQuestion>
                actionRef={tableRef}
                headerTitle="List Questions "
                rowKey="id"
                loading={loading}
                columns={columns}
                dataSource={dataSource}
                scroll={{ x: true }}
                pagination={{
                    current: currentPage,
                    total: total,
                    pageSize: pageSize,
                    onChange: (page, size) => {
                        setCurrentPage(page); // Cập nhật trang hiện tại
                        setPageSize(size); // Cập nhật kích thước trang
                    },
                    showSizeChanger: true, // Cho phép thay đổi kích thước trang
                }}
                rowSelection={{
                    type: 'checkbox',
                    selectedRowKeys,
                    onChange: (keys) => {
                        setSelectedRowKeys(keys);
                        console.log('Selected keys:', keys);
                    }
                }}
                toolBarRender={() => [
                    <Button
                        key="add"
                        icon={<PlusOutlined />}
                        type="primary"
                        onClick={() => setOpenModal(true)}
                    >
                        Add Question
                    </Button>,
                    <Dropdown
                        key="lesson"
                        menu={{
                            items: lessonList.map(lesson => ({
                                key: lesson.id,
                                label: lesson.name,
                                onClick: () => handleQuestionLesson(lesson.id, 'add')
                            }))
                        }}
                    >
                        <Button>Add to Lesson</Button>
                    </Dropdown>
                ]}
            />
            <ModalQuestion
                openModal={openModal}
                setOpenModal={setOpenModal}
                reloadTable={reloadTable}
                singleQuestion={dataInit}
                setSingleQuestion={setDataInit}
            />

            <ModalUpload
                openModalUpload={openModalUpload}
                setOpenModalUpload={setOpenModalUpload}
                reloadTable={reloadTable}
                uploadData={uploadData}
                quesID={quesID}
            />

            <ModalSampleAnswer
                openModal={openModalSampleAnswer}
                setOpenModal={setOpenModalSampleAnswer}
                questionId={quesID}
                questionData={dataInit}
            />
        </div>
    )
}

export default () => (
    <Access permission={{ module: "CONTENT_MANAGEMENT" }}>
        <App>
            <QuestionPage />
        </App>
    </Access>
);