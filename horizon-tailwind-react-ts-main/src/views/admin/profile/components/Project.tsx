import React, { useState, useEffect } from "react";
import { MdModeEditOutline } from "react-icons/md";
import image1 from "assets/img/profile/image1.png";
import Card from "components/card";
import LessonList from "./LessonList";
import { useAuth } from "hooks/useAuth";
import EditCourse from "./EditCourses";
import { API_BASE_URL } from "service/api.config";
import Access from "views/admin/access";
import { ALL_PERMISSIONS } from "views/admin/permission/components/modules";
import { message } from "antd";

type RowObj = {
  id: number;
  name: string;
  intro: string;
  diffLevel: number;
  recomLevel: number;
  courseType: string;
  speciField: string;
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
  const fetchCourses = async () => {
    // Thay thế bằng API thực tế để lấy danh sách khóa học
    const response = await fetch(`${API_BASE_URL}/api/v1/courses`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    const data = await response.json();
    console.log(data.data.content);
    setCourses(data.data.content);
  };

  useEffect(() => {
    fetchCourses();
  }, []);
  const handleSuccess = () => {
    fetchCourses(); // Tải lại danh sách khóa học sau khi submit
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
      const response = await fetch(`${API_BASE_URL}/api/v1/lessons?page=1&size=10`, {
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
            All projects
          </h4>
          <p className="mt-2 text-base text-gray-600">
            Here you can find more details about your projects. Keep you user
            engaged by providing meaningful information.
          </p>
        </div>
        {/* Project 1
        <div className="flex w-full items-center justify-between rounded-2xl bg-white p-3 shadow-3xl shadow-shadow-500 dark:!bg-navy-700 dark:shadow-none">
          <div className="flex items-center">
            <div className="">
              <img className="h-[83px] w-[83px] rounded-lg" src={image1} alt="" />
            </div>
            <div className="ml-4">
              <p className="text-base font-medium text-navy-700 dark:text-white">
                Technology behind the Blockchain
              </p>
              <p className="mt-2 text-sm text-gray-600">
                Project #1 .
                <a
                  className="ml-1 font-medium text-brand-500 hover:text-brand-500 dark:text-white"
                  href=" "
                >
                  See product details
                </a>
              </p>
            </div>
          </div>
          <div className="mr-4 flex items-center justify-center text-gray-600 dark:text-white">
            <MdModeEditOutline />
          </div>
          <div className="mr-4 flex items-center justify-center text-gray-600 dark:text-white">
            <button
              className="bg-blue-500 text-white px-4 py-2 rounded"
              onClick={handleAddCourse}
            >Add</button>
          </div>
        </div> */}
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
        <div className="mt-0 overflow-x-scroll xl:overflow-x-hidden">
          <table className="w-full">
            <thead>
              <tr className="!border-px !border-gray-400">
                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">ID</th>
                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Name</th>
                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Intro</th>
                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Diff Level</th>
                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Recom Level</th>
                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Course Type</th>
                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Special Field</th>
                <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Actions</th>
              </tr>
            </thead>
            <tbody>
              {courses.map(course => (
                <tr key={course.id}>
                  <td className="border-b border-gray-200 py-3 pr-4">{course.id}</td>
                  <td className="border-b border-gray-200 py-3 pr-4">{course.name}</td>
                  <td className="border-b border-gray-200 py-3 pr-4">{course.intro}</td>
                  <td className="border-b border-gray-200 py-3 pr-4">{course.diffLevel}</td>
                  <td className="border-b border-gray-200 py-3 pr-4">{course.recomLevel}</td>
                  <td className="border-b border-gray-200 py-3 pr-4">{course.courseType}</td>
                  <td className="border-b border-gray-200 py-3 pr-4">{course.speciField}</td>
                  <td className="border-b border-gray-200 py-3 pr-4">
                    <button onClick={() => handleAddLesson(course.id)} className="text-blue-500 hover:underline ml-2">Add</button>
                    <button onClick={() => handleEdit(course.id)} className="text-yellow-500 hover:underline ml-2">Edit</button>
                    <button onClick={() => handleDelete(course)} className="text-red-500 hover:underline ml-2">Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

      </Card>
    </Access>
  );
};

export default Project;
