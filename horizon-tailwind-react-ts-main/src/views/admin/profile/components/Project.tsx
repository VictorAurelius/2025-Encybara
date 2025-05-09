import React, { useState, useEffect } from "react";
import { MdAddCircleOutline, MdDelete, MdModeEditOutline } from "react-icons/md";
import Card from "components/card";
import LessonList from "./LessonList";
import { useAuth } from "hooks/useAuth";
import EditCourse from "./EditCourses";
import { API_BASE_URL } from "service/api.config";
import Access from "views/admin/access";
import { message, Pagination } from "antd";
import { Select, Input, Button, Space, Row, Col } from 'antd';
import { SearchOutlined } from "@ant-design/icons";
type RowObj = {
  id: number;
  name: string;
  intro: string;
  diffLevel: number;
  recomLevel: number;
  courseType: string;
  speciField: string;
  courseStatus: string; // Thuộc tính mới
  group: string; // Thuộc tính mới
};

interface ProjectProps {
  tableData: RowObj[];
}

const Project: React.FC<ProjectProps> = ({ tableData }) => {
  const { token } = useAuth();
  const [showModal, setShowModal] = useState(false);
  const [lessons, setLessons] = useState<any[]>([]);
  const [loadingLessons, setLoadingLessons] = useState(true);
  const [errorLessons, setErrorLessons] = useState<string | null>(null);
  const [editingCourseId, setEditingCourseId] = useState<number | null>(null);
  const [courseId, setCourseId] = useState<number | null>(null);
  const [showEditCourse, setShowEditCourse] = useState(false);
  const [courses, setCourses] = useState<RowObj[]>(tableData);
  const [selectedFilters, setSelectedFilters] = useState({
    diffLevel: undefined,
    courseType: undefined,
    group: undefined,
    courseStatus: undefined,
    keyword: undefined
  });
  const [pageSize, setPageSize] = useState<number>(10);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [total, setTotal] = useState<number>(0);
  const [groupOptions, setGroupOptions] = useState<string[]>([]);

  const fetchCourses = async () => {
    try {
      // Xây dựng query params
      const queryParams = new URLSearchParams({
        page: currentPage.toString(),
        size: pageSize.toString()
      });

      // Thêm filter params từ state vào URL
      if (selectedFilters.diffLevel) queryParams.append('diffLevel', selectedFilters.diffLevel);
      if (selectedFilters.courseType) queryParams.append('courseType', selectedFilters.courseType);
      if (selectedFilters.group) queryParams.append('group', selectedFilters.group);
      if (selectedFilters.courseStatus) queryParams.append('courseStatus', selectedFilters.courseStatus);
      if (selectedFilters.keyword) queryParams.append('keyword', selectedFilters.keyword);

      // Gọi API
      const response = await fetch(`${API_BASE_URL}/api/v1/courses?${queryParams}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch courses");
      }

      const data = await response.json();
      console.log(data.data.content);
      setCourses(data.data.content);
      setTotal(data.data.totalPages * pageSize);
    } catch (error) {
      console.error("Error fetching courses:", error);
      message.error("Failed to fetch courses");
    }
  };

  const fetchGroups = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/courses/groups`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch course groups");
      }

      const data = await response.json();
      if (data.data && data.data.content) {
        setGroupOptions(data.data.content);
      }
    } catch (error) {
      console.error("Error fetching course groups:", error);
      message.error("Failed to fetch course groups");
    }
  };

  const resetFilters = () => {
    setSelectedFilters({
      diffLevel: undefined,
      courseType: undefined,
      group: undefined,
      courseStatus: undefined,
      keyword: undefined
    });
  };

  useEffect(() => {
    fetchGroups();
    fetchCourses();
  }, [courses]);

  useEffect(() => {
    fetchCourses();
  }, [currentPage, pageSize]);

  const handleSuccess = () => {
    fetchCourses(); // Tải lại danh sách khóa học sau khi submit
  };
  // Cập nhật hàm xử lý chuyển đổi status
  const handleToggleStatus = async (course: RowObj) => {
    try {
      let endpoint;
      let newStatus;

      // Xác định endpoint và message dựa trên trạng thái hiện tại
      if (course.courseStatus === "PUBLIC") {
        endpoint = `${API_BASE_URL}/api/v1/courses/${course.id}/make-private`;
        newStatus = "private";
      } else if (course.courseStatus === "PENDING") {
        endpoint = `${API_BASE_URL}/api/v1/courses/${course.id}/publish`;
        newStatus = "public";
      } else {
        endpoint = `${API_BASE_URL}/api/v1/courses/${course.id}/make-public`;
        newStatus = "public";
      }

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`Failed to change course status to ${newStatus}`);
      }

      // Cập nhật UI
      message.success(`Course status changed to ${newStatus} successfully`);

      // Tải lại danh sách khóa học
      await fetchCourses();
    } catch (error) {
      console.error("Error changing course status:", error);
      message.error("Failed to change course status");
    }
  };
  const handleAddLesson = async (courseId: number) => {
    setCourseId(courseId);
    setShowModal(true);
    await fetchLessons();
  };
  const handleAddCourse = () => {
    setEditingCourseId(null); // Đặt courseId là null
    setShowEditCourse(true); // Hiển thị modal
  };
  const fetchLessons = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/lessons?page=1&size=1000`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch lessons");
      }

      const data = await response.json();
      setLessons(data.data.content);
    } catch (error) {
      console.error("Error fetching lessons:", error);
      setErrorLessons("Failed to fetch lessons");
    } finally {
      setLoadingLessons(false);
    }
  };
  const handleEdit = (id: number) => {
    setEditingCourseId(id);
    setShowEditCourse(true);
  };
  const handleCloseEdit = () => {
    setShowEditCourse(false); // Đóng modal
    fetchCourses();
  };

  const handleDelete = async (row: RowObj) => {
    const res = await fetch(`${API_BASE_URL}/api/v1/courses/${row.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    if (res.ok) {
      fetchCourses();
      message.success("Delete course successfully");
    } else {
      message.error("Failed to delete course");
    }
  };

  return (
    <Access
      permission={{ module: "CONTENT_MANAGEMENT" }}
    >
      <Card extra={"w-full p-4 h-full"}>
        <div className="mb-8 w-full">
          <h4 className="text-xl font-bold text-navy-700 dark:text-white">
            All Courses
          </h4>
          <p className="mt-2 text-base text-gray-600">
            Here you can find more details about your courses. Keep your users
            engaged by providing meaningful information.
          </p>
        </div>

        {/* Filter section - centered */}
        <div className="mb-6 flex flex-col items-center">
          <div className="w-full max-w-4xl">
            <div className="flex justify-center mb-4">
              <Button
                type="primary"
                size="large"
                onClick={handleAddCourse}
                className="bg-blue-500 hover:bg-blue-600"
              >
                Add New Course
              </Button>
            </div>



            <Row gutter={16} className="mb-4">
              <Col span={6}>
                <Select
                  placeholder="Difficulty Level"
                  style={{ width: '100%' }}
                  value={selectedFilters.diffLevel}
                  onChange={(value) => setSelectedFilters({ ...selectedFilters, diffLevel: value })}
                  allowClear
                  options={[
                    { value: '1.0', label: 'Level 1' },
                    { value: '1.5', label: 'Level 2' },
                    { value: '2.0', label: 'Level 3' },
                    { value: '2.5', label: 'Level 4' },
                    { value: '3.0', label: 'Level 5' },
                    { value: '3.5', label: 'Level 6' },
                    { value: '4.0', label: 'Level 7' },
                    { value: '4.5', label: 'Level 8' },
                    { value: '5.0', label: 'Level 9' },
                    { value: '5.5', label: 'Level 10' },
                  ]}
                />
              </Col>
              <Col span={6}>
                <Select
                  placeholder="Course Type"
                  style={{ width: '100%' }}
                  value={selectedFilters.courseType}
                  onChange={(value) => setSelectedFilters({ ...selectedFilters, courseType: value })}
                  allowClear
                  options={[
                    { value: 'READING', label: 'Reading' },
                    { value: 'LISTENING', label: 'Listening' },
                    { value: 'WRITING', label: 'Writing' },
                    { value: 'SPEAKING', label: 'Speaking' },
                    { value: 'ALLSKILLS', label: 'AllSkills' }
                  ]}
                />
              </Col>
              <Col span={6}>
                <Select
                  placeholder="Select Group"
                  style={{ width: '100%' }}
                  value={selectedFilters.group}
                  onChange={(value) => setSelectedFilters({ ...selectedFilters, group: value })}
                  allowClear
                  showSearch
                  optionFilterProp="children"
                  filterOption={(input, option) =>
                    (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                  }
                  options={groupOptions.map(group => ({ value: group, label: group }))}
                  loading={groupOptions.length === 0}
                />
              </Col>
              <Col span={6}>
                <Select
                  placeholder="Status"
                  style={{ width: '100%' }}
                  value={selectedFilters.courseStatus}
                  onChange={(value) => setSelectedFilters({ ...selectedFilters, courseStatus: value })}
                  allowClear
                  options={[
                    { value: 'PUBLIC', label: 'Public' },
                    { value: 'PRIVATE', label: 'Private' },
                    { value: 'PENDING', label: 'Pending' },
                  ]}
                />
              </Col>
            </Row>

            <Row>
              <Col span={24} className="flex justify-center gap-4">
                <Button
                  onClick={resetFilters}
                  size="large"
                >
                  Reset
                </Button>
                <Button
                  type="primary"
                  onClick={fetchCourses}
                  size="large"
                  className="px-8"
                >
                  Filter
                </Button>
              </Col>
            </Row>
          </div>
        </div>

        {showModal && (
          <div className="fixed inset-0 z-5 ml-20 flex items-center justify-center  mt-20">
            <div className="fixed inset-0  bg-gray-800 bg-opacity-75 opacity-50"></div>
            <div className="relative bg-white rounded-lg shadow-xl p-6 w-full max-w-2xl mx-4">
              <LessonList lessons={lessons} courseId={courseId} fetchLessons={fetchLessons} />
              <div className="flex justify-end mt-4"> {/* Căn nút Close sang phải */}
                <button
                  onClick={() => setShowModal(false)}
                  className="bg-red-500 text-white px-6 py-2 rounded-md hover:bg-red-600 transition-colors"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        )}
        {showEditCourse && (
          <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
            <div className="bg-white p-6 rounded-lg w-3/4 max-w-4xl mt-10">
              <EditCourse courseId={editingCourseId} onClose={handleCloseEdit} onSuccess={handleSuccess} />
            </div>
          </div>
        )}

        <div className="w-full">
          <div className="overflow-hidden rounded-lg border border-gray-200 shadow-md">
            <div className="overflow-x-auto">
              <table className="w-full table-fixed border-collapse bg-white text-left text-sm text-gray-500">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[3%]">ID</th>
                    <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[17%]">Name</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[33%]">Intro</th>
                    <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[6%]">Diff Level</th>
                    <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[6%]">Recom Level</th>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[6%]">Course Type</th>
                    {/* <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[6%]">Special Field</th> */}
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[9%]">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[10%]">Group</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[14%]">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 border-t border-gray-100">
                  {courses.map((course, index) => (
                    <tr key={course.id} className={index % 2 === 0 ? "bg-white" : "bg-gray-50"}>
                      <td className="px-2 py-4 whitespace-nowrap text-sm text-gray-900">{course.id}</td>
                      <td className="px-2 py-4 text-sm text-gray-900">
                        <div className="font-medium text-gray-700">{course.name}</div>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        <div className="max-h-24 overflow-y-auto pr-2">
                          {course.intro}
                        </div>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-900">{course.diffLevel}</td>
                      <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-900">{course.recomLevel}</td>
                      <td className="px-2 py-4 whitespace-nowrap text-sm text-gray-900">{course.courseType}</td>
                      {/* <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{course.speciField}</td> */}
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <button
                          onClick={() => handleToggleStatus(course)}
                          className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full cursor-pointer hover:opacity-80 ${course.courseStatus === "PUBLIC"
                            ? "bg-green-100 text-green-800 hover:bg-green-200"
                            : course.courseStatus === "DRAFT"
                              ? "bg-yellow-100 text-yellow-800 hover:bg-yellow-200"
                              : "bg-red-100 text-red-800 hover:bg-red-200"
                            }`}
                          title={`Click to change to ${course.courseStatus === "PUBLIC" ? "Private" : "Public"}`}
                        >
                          {course.courseStatus}
                        </button>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">{course.group}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <div className="flex space-x-2">
                          <button
                            onClick={() => handleAddLesson(course.id)}
                            className="text-blue-600 hover:text-blue-900"
                          >
                            <MdAddCircleOutline size={20} />
                          </button>
                          <button
                            onClick={() => handleEdit(course.id)}
                            className="text-yellow-600 hover:text-yellow-900"
                          >
                            <MdModeEditOutline size={20} />
                          </button>

                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div className="mt-4 flex justify-end">
          <Pagination
            current={currentPage}
            total={total}
            pageSize={pageSize}
            onChange={(page, size) => {
              setCurrentPage(page);
              if (size) setPageSize(size);
            }}
            showSizeChanger
            showTotal={(total) => `Total ${total} items`}
          />
        </div>

      </Card>
    </Access>
  );
};

export default Project;
