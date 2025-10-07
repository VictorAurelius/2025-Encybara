import React, { useEffect, useState } from "react";
import { Button, Table, Upload, message, Popconfirm, Modal, Spin, Tooltip, Space, Select, Form } from "antd";
import { UploadOutlined, DeleteOutlined, FileTextOutlined, EyeOutlined, InfoCircleOutlined, PlusOutlined } from "@ant-design/icons";
import lectureService, { Course, LectureMaterial } from "../../../service/lecture.service";
import { useCache } from "../../../hooks/useCache";
import { useAuth } from "hooks/useAuth";
import profileService from "../../../service/profile.service";
import 'styles/markdown.css';
const LecturePage: React.FC = () => {
  const { token } = useAuth();
  const { getCacheStats } = useCache();
  const [materials, setMaterials] = useState<LectureMaterial[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [courses, setCourses] = useState<Course[]>([]);
  const [allCourses, setAllCourses] = useState<Course[]>([]);
  const [coursesWithoutMaterials, setCoursesWithoutMaterials] = useState<Course[]>([]);
  const [markdownVisible, setMarkdownVisible] = useState<boolean>(false);
  const [markdownContent, setMarkdownContent] = useState<string>('');
  const [markdownLoading, setMarkdownLoading] = useState<boolean>(false);
  const [currentFileName, setCurrentFileName] = useState<string>('');
  
  // Upload modal states
  const [uploadModalVisible, setUploadModalVisible] = useState<boolean>(false);
  const [uploadLoading, setUploadLoading] = useState<boolean>(false);
  const [selectedCourseForUpload, setSelectedCourseForUpload] = useState<number | undefined>(undefined);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  // Fetch courses with materials
  const fetchCourses = async () => {
    try {
      setLoading(true);
      const coursesData = await lectureService.getCoursesWithMaterials(token);
      setCourses(coursesData);
    } catch (error) {
      console.error('Error fetching courses:', error);
      message.error('Failed to fetch courses');
    } finally {
      setLoading(false);
    }
  };

  // Fetch all courses for upload modal
  const fetchAllCourses = async () => {
    try {
      // Use profileService to get courses with larger page size to get all courses
      const response = await profileService.getCourses({}, { page: 1, size: 1000 });
      const allCoursesData = response.content || [];
      
      // Ensure it's always an array
      setAllCourses(Array.isArray(allCoursesData) ? allCoursesData : []);
    } catch (error) {
      console.error('Error fetching all courses:', error);
      message.error('Failed to fetch courses');
      // Set empty array on error to prevent map error
      setAllCourses([]);
    }
  };

  // Fetch all materials from all courses
  const fetchMaterials = async () => {
    try {
      setLoading(true);      
      const allMaterials: LectureMaterial[] = [];
      const coursesWithMaterials: number[] = [];
      
      for (const course of courses) {
        try {
          const courseMaterials = await lectureService.getMaterialsByCourseId(course.id, token);
          if (courseMaterials.length > 0) {
            // Add courseId to each material
            const materialsWithCourseId = courseMaterials.map(material => ({
              ...material,
              courseId: course.id
            }));
            allMaterials.push(...materialsWithCourseId);
            coursesWithMaterials.push(course.id);
          }
        } catch (error) {
          console.error(`Error fetching materials for course ${course.id}:`, error);
        }
      }
      
      // Find courses without materials
      const coursesWithoutMats = courses.filter(course => 
        !coursesWithMaterials.includes(course.id)
      );
      
      setMaterials(allMaterials);
      setCoursesWithoutMaterials(coursesWithoutMats);
    } catch (error) {
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
      await lectureService.deleteMaterial(id, courseId);
      message.success("Lecture deleted successfully");
      fetchMaterials();
    } catch (error) {
      console.error("Error deleting lecture:", error);
      message.error("Failed to delete lecture");
    }
  };

  // Upload modal handlers
  const openUploadModal = async () => {
    setUploadModalVisible(true);
    await fetchAllCourses();
  };

  const closeUploadModal = () => {
    setUploadModalVisible(false);
    setSelectedCourseForUpload(undefined);
    setUploadFile(null);
  };

  const handleModalUpload = async () => {
    if (!selectedCourseForUpload || !uploadFile) {
      message.error('Please select a course and file');
      return;
    }

    try {
      setUploadLoading(true);
      await lectureService.uploadMaterial(uploadFile, selectedCourseForUpload);
      message.success('Material uploaded successfully');
      closeUploadModal();
      fetchMaterials();
    } catch (error) {
      console.error('Error uploading material:', error);
      message.error('Failed to upload material');
    } finally {
      setUploadLoading(false);
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

  return (
    <div className="mt-3 grid h-full">
      <div className="w-full rounded-[20px] bg-white p-4">
        <div className="mb-6">
          <div className="flex justify-between items-center mb-4">
            <h4 className="text-xl font-bold text-navy-700">Lecture Materials</h4>
            
            <div className="flex gap-2 items-center">
              <div className="text-sm text-gray-500 flex items-center mr-4">
                Total: {materials.length} materials from {courses.length} courses
              </div>
              <Button 
                type="primary" 
                icon={<UploadOutlined />}
                onClick={openUploadModal}
                className="bg-brand-500 hover:bg-brand-600"
              >
                Upload Material
              </Button>
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
              width: '35%',
              render: (_, record: LectureMaterial) => {
                // Find the course this material belongs to
                const course = allCourses.find(c => c.id === record.courseId) || 
                              courses.find(c => c.id === record.courseId) || 
                              { id: record.courseId || 0, name: 'Unknown Course' };
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
              width: '15%',
              render: (_, record: LectureMaterial) => (
                <Space size="middle">      
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
                      const courseId = record.courseId || courses[0]?.id || 1;
                      handleDelete(record.id, courseId);
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

        {/* Upload Material Modal */}
        <Modal
          title="Upload Material"
          open={uploadModalVisible}
          onCancel={closeUploadModal}
          onOk={handleModalUpload}
          confirmLoading={uploadLoading}
          width={600}
        >
          <Form layout="vertical" className="mt-4">
            <Form.Item label="Select Course" required>
              <Select
                placeholder="Choose a course"
                value={selectedCourseForUpload}
                onChange={setSelectedCourseForUpload}
                showSearch
                optionFilterProp="children"
                className="w-full"
              >
                {(allCourses || []).map((course: Course) => (
                  <Select.Option key={course.id} value={course.id}>
                    {course.name || `Course ${course.id}`}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            
            <Form.Item label="Select File" required>
              <Upload
                accept=".md"
                beforeUpload={(file: File) => {
                  setUploadFile(file);
                  return false;
                }}
                maxCount={1}
                onRemove={() => setUploadFile(null)}
              >
                <Button icon={<UploadOutlined />}>
                  Choose Markdown File
                </Button>
              </Upload>
              {uploadFile && (
                <div className="mt-2 text-sm text-green-600">
                  📄 Selected: {uploadFile.name}
                </div>
              )}
            </Form.Item>

            {selectedCourseForUpload && (
              <div className="mt-4 p-3 bg-blue-50 rounded border">
                <div className="text-sm text-blue-700">
                  <strong>Note:</strong> This will upload material to{' '}
                  <strong>
                    {allCourses.find(c => c.id === selectedCourseForUpload)?.name || `Course ${selectedCourseForUpload}`}
                  </strong>
                  . If a material already exists, it will be replaced.
                </div>
              </div>
            )}
          </Form>
        </Modal>
      </div>
    </div>
  );
};

export default LecturePage;