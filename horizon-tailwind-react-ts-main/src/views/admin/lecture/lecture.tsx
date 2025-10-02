import React, { useEffect, useState } from "react";
import { Button, Table, Upload, message, Popconfirm, Modal, Spin, Tooltip, Space } from "antd";
import { UploadOutlined, DeleteOutlined, FileTextOutlined, EyeOutlined, InfoCircleOutlined } from "@ant-design/icons";
import lectureService, { Course, LectureMaterial } from "../../../service/lecture.service";
import { useCache } from "../../../hooks/useCache";
import { useAuth } from "hooks/useAuth";
import 'styles/markdown.css';
const LecturePage: React.FC = () => {
  const { token } = useAuth();
  const { getCacheStats } = useCache();
  const [materials, setMaterials] = useState<LectureMaterial[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [courses, setCourses] = useState<Course[]>([]);
  const [markdownVisible, setMarkdownVisible] = useState<boolean>(false);
  const [markdownContent, setMarkdownContent] = useState<string>('');
  const [markdownLoading, setMarkdownLoading] = useState<boolean>(false);
  const [currentFileName, setCurrentFileName] = useState<string>('');
  // Fetch courses with materials
  const fetchCourses = async () => {
    try {
      setLoading(true);
      console.log('📋 Fetching courses with materials...');
      const coursesData = await lectureService.getCoursesWithMaterials(token);
      setCourses(coursesData);
    } catch (error) {
      console.error('Error fetching courses:', error);
      message.error('Failed to fetch courses');
    } finally {
      setLoading(false);
    }
  };

  // Fetch all materials from all courses
  const fetchMaterials = async () => {
    try {
      setLoading(true);
      console.log('📄 Fetching all materials from all courses...');
      
      const allMaterials: LectureMaterial[] = [];
      for (const course of courses) {
        try {
          const courseMaterials = await lectureService.getMaterialsByCourseId(course.id, token);
          allMaterials.push(...courseMaterials);
        } catch (error) {
          console.error(`Error fetching materials for course ${course.id}:`, error);
        }
      }
      
      setMaterials(allMaterials);
    } catch (error) {
      console.error("Error fetching materials:", error);
      message.error("Failed to fetch lecture materials");
    } finally {
      setLoading(false);
    }
  };

  // Read markdown content
  const readMarkdown = async (materLink: string, fileName: string) => {
    try {
      setMarkdownLoading(true);
      setCurrentFileName(fileName);
      console.log(`� Reading markdown content from: ${materLink}`);
      const htmlContent = await lectureService.renderMarkdownToHtml(materLink);
      setMarkdownContent(htmlContent);
      setMarkdownVisible(true);
    } catch (error) {
      console.error('Error reading markdown:', error);
      message.error('Failed to read markdown file');
    } finally {
      setMarkdownLoading(false);
    }
  };

  const handleUpload = async (file: File, courseId: number) => {
    try {
      console.log(`📤 Uploading file for course ${courseId}`);
      await lectureService.uploadMaterial(file, courseId);
      message.success("Lecture uploaded successfully");
      fetchMaterials();
    } catch (error) {
      console.error("Error uploading lecture:", error);
      message.error("Failed to upload lecture");
    }
  };

  const handleDelete = async (id: number, courseId: number) => {
    try {
      console.log(`🗑️ Deleting material ${id} from course ${courseId}`);
      await lectureService.deleteMaterial(id, courseId);
      message.success("Lecture deleted successfully");
      fetchMaterials();
    } catch (error) {
      console.error("Error deleting lecture:", error);
      message.error("Failed to delete lecture");
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  useEffect(() => {
    if (courses.length > 0) {
      fetchMaterials();
    }
  }, [courses]);

      console.log('Available courses:', courses);
  
  return (
    <div className="mt-3 grid h-full">
      <div className="w-full rounded-[20px] bg-white p-4">
        <div className="mb-6">
          <div className="flex justify-between items-center mb-4">
            <h4 className="text-xl font-bold text-navy-700">Lecture Materials</h4>
            
            <div className="flex gap-2">
              <div className="text-sm text-gray-500 flex items-center mr-4">
                Total: {materials.length} materials from {courses.length} courses
              </div>
            </div>
          </div>
        </div>

        <Table<LectureMaterial>
          loading={loading}
          dataSource={materials}
          rowKey="id"
          locale={{ emptyText: 'No lecture materials found' }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} of ${total} items`
          }}
          columns={[
            {
              title: 'Course',
              key: 'course',
              width: '25%',
              render: (_, record: LectureMaterial) => {
                // For now, we'll use the first course as default since we don't have courseId in LectureMaterial
                const course = courses[0] || { id: 1, name: 'Default Course' };
                return (
                  <div className="flex items-center">
                    <div className="w-3 h-3 bg-blue-500 rounded-full mr-3"></div>
                    <span className="font-medium text-navy-700">
                      {course?.name || `Course ${course.id}`}
                    </span>
                  </div>
                );
              }
            },
            {
              title: 'Material',
              dataIndex: 'materLink',
              key: 'material',
              width: '45%',
              render: (materLink: string, record: LectureMaterial) => (
                <div>
                  <div className="flex items-center mb-1">
                    <FileTextOutlined className="text-blue-500 mr-2" />
                    <span className="font-medium text-gray-800">
                      {lectureService.getFileName(materLink)}
                    </span>
                  </div>
                  <div className="text-sm text-gray-500">
                    <Tooltip title={new Date(record.uploadedAt).toLocaleString()}>
                      Uploaded: {new Date(record.uploadedAt).toLocaleDateString()}
                    </Tooltip>
                  </div>
                </div>
              )
            },
            {
              title: 'Actions',
              key: 'actions',
              width: '30%',
              render: (_, record: LectureMaterial) => (
                <Space size="middle">
                  <Upload
                    accept=".md"
                    showUploadList={false}
                    beforeUpload={(file: File) => {
                      const defaultCourseId = courses[0]?.id || 1;
                      handleUpload(file, defaultCourseId);
                      return false;
                    }}
                  >
                    <Button 
                      icon={<UploadOutlined />}
                      size="small"
                      className="bg-green-500 text-white hover:bg-green-600 border-green-500"
                    >
                      Upload
                    </Button>
                  </Upload>
                  
                  <Button
                    type="primary"
                    icon={<EyeOutlined />}
                    size="small"
                    onClick={() => readMarkdown(record.materLink, lectureService.getFileName(record.materLink))}
                    loading={markdownLoading}
                  >
                    View
                  </Button>
                  
                  <Popconfirm
                    title="Are you sure you want to delete this lecture?"
                    onConfirm={() => {
                      const defaultCourseId = courses[0]?.id || 1;
                      handleDelete(record.id, defaultCourseId);
                    }}
                    okText="Yes"
                    cancelText="No"
                  >
                    <Button 
                      danger 
                      icon={<DeleteOutlined />} 
                      size="small"
                    >
                      Delete
                    </Button>
                  </Popconfirm>
                </Space>
              )
            }
          ]}
        />

        {/* Markdown Viewer Modal */}
        <Modal
          title={`Viewing: ${currentFileName}`}
          open={markdownVisible}
          onCancel={() => setMarkdownVisible(false)}
          footer={null}
          width="80%"
          style={{ top: 20 }}
        >
          <Spin spinning={markdownLoading}>
            <div className="max-h-[70vh] overflow-y-auto">
              <div 
                className="markdown-content"
                dangerouslySetInnerHTML={{ __html: markdownContent }}
              />
            </div>
          </Spin>
        </Modal>
      </div>
    </div>
  );
};

export default LecturePage;