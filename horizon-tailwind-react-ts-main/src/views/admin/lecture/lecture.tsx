import React, { useEffect, useState } from "react";
import { Button, List, Upload, message, Popconfirm, Select, Card, Modal, Spin, Tooltip } from "antd";
import { UploadOutlined, DeleteOutlined, FileTextOutlined, EyeOutlined, InfoCircleOutlined } from "@ant-design/icons";
import lectureService, { Course, LectureMaterial } from "../../../service/lecture.service";
import { useCache } from "../../../hooks/useCache";

const LecturePage: React.FC = () => {
  const { getCacheStats } = useCache();
  const [materials, setMaterials] = useState<LectureMaterial[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [selectedCourse, setSelectedCourse] = useState<number | null>(null);
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
      const coursesData = await lectureService.getCoursesWithMaterials();
      setCourses(coursesData);
    } catch (error) {
      console.error('Error fetching courses:', error);
      message.error('Failed to fetch courses');
    } finally {
      setLoading(false);
    }
  };

  // Fetch materials for selected course
  const fetchMaterials = async (courseId: number) => {
    try {
      setLoading(true);
      console.log(`📄 Fetching materials for course ${courseId}...`);
      const materials = await lectureService.getMaterialsByCourseId(courseId);
      setMaterials(materials);
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
      console.log(`📄 Reading markdown content from: ${materLink}`);
      const content = await lectureService.readMarkdownContent(materLink);
      setMarkdownContent(content);
      setMarkdownVisible(true);
    } catch (error) {
      console.error('Error reading markdown:', error);
      message.error('Failed to read markdown file');
    } finally {
      setMarkdownLoading(false);
    }
  };

  const handleUpload = async (file: File) => {
    if (!selectedCourse) {
      message.error('Please select a course first');
      return false;
    }

    try {
      console.log(`📤 Uploading file for course ${selectedCourse}`);
      await lectureService.uploadMaterial(file, selectedCourse);
      message.success("Lecture uploaded successfully");
      fetchMaterials(selectedCourse);
    } catch (error) {
      console.error("Error uploading lecture:", error);
      message.error("Failed to upload lecture");
    }
  };

  const handleDelete = async (id: number) => {
    if (!selectedCourse) return;
    
    try {
      console.log(`🗑️ Deleting material ${id}`);
      await lectureService.deleteMaterial(id, selectedCourse);
      message.success("Lecture deleted successfully");
      fetchMaterials(selectedCourse);
    } catch (error) {
      console.error("Error deleting lecture:", error);
      message.error("Failed to delete lecture");
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  useEffect(() => {
    if (selectedCourse) {
      fetchMaterials(selectedCourse);
    }
  }, [selectedCourse]);

  const handleCourseChange = (value: number) => {
    console.log(`📋 Selected course: ${value}`);
    setSelectedCourse(value);
    setMaterials([]);
  };

      console.log('Available courses:', courses);
  console.log('Selected course ID:', selectedCourse);
  
  return (
    <div className="mt-3 grid h-full">
      <div className="w-full rounded-[20px] bg-white p-4">
        <div className="mb-6">
          <div className="flex justify-between items-center mb-4">
            <h4 className="text-xl font-bold text-navy-700">Lecture Materials</h4>
            
            <div className="flex gap-2">
              <Tooltip title={`Cache: ${getCacheStats().size} items`}>
                <Button 
                  icon={<InfoCircleOutlined />}
                  size="small"
                  onClick={() => {
                    const stats = lectureService.getLectureCacheStats();
                    message.info(`Lecture cache: ${stats.size} items`);
                  }}
                >
                  Cache
                </Button>
              </Tooltip>
              
              <Button 
                size="small"
                onClick={() => {
                  lectureService.clearAllLectureCache();
                  message.success('Lecture cache cleared');
                  fetchCourses();
                }}
                type="dashed"
              >
                Clear Cache
              </Button>
            </div>
          </div>
          
          <div className="flex gap-4 mb-4">
            <Select<number>
              className="w-64"
              placeholder="Select Course"
              value={selectedCourse}
              onChange={handleCourseChange}
              showSearch
              optionFilterProp="children"
            >
              {courses.map((course: Course) => (
                <Select.Option key={course.id} value={course.id}>
                  {course.name || `Course ${course.id}`}
                </Select.Option>
              ))}
            </Select>

            <Upload
              accept=".md"
              showUploadList={false}
              beforeUpload={(file: File) => {
                handleUpload(file);
                return false;
              }}
              disabled={!selectedCourse}
            >
              <Button 
                icon={<UploadOutlined />}
                disabled={!selectedCourse}
                className="bg-blue-500 text-white hover:bg-blue-600"
              >
                Upload Lecture
              </Button>
            </Upload>
          </div>
        </div>

        <List<LectureMaterial>
          loading={loading}
          dataSource={materials}
          grid={{ gutter: 16, column: 3 }}
          locale={{ emptyText: selectedCourse ? 'No lectures found' : 'Please select a course' }}
          renderItem={(item: LectureMaterial) => (
            <List.Item key={item.id}>
              <Card
                hoverable
                className="shadow-sm"
                actions={[
                  <Button
                    key="view"
                    type="text"
                    icon={<EyeOutlined />}
                    onClick={() => readMarkdown(item.materLink, lectureService.getFileName(item.materLink))}
                    loading={markdownLoading}
                  >
                    View
                  </Button>,
                  <Popconfirm
                    key="delete"
                    title="Are you sure you want to delete this lecture?"
                    onConfirm={() => handleDelete(item.id)}
                    okText="Yes"
                    cancelText="No"
                  >
                    <Button type="text" icon={<DeleteOutlined />} danger />
                  </Popconfirm>
                ]}
              >
                <Card.Meta
                  avatar={<FileTextOutlined className="text-2xl text-blue-500" />}
                  title={lectureService.getFileName(item.materLink)}
                  description={`Uploaded: ${new Date(item.uploadedAt).toLocaleString()}`}
                />
              </Card>
            </List.Item>
          )}
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
              <pre className="whitespace-pre-wrap font-mono text-sm bg-gray-50 p-4 rounded">
                {markdownContent}
              </pre>
            </div>
          </Spin>
        </Modal>
      </div>
    </div>
  );
};

export default LecturePage;