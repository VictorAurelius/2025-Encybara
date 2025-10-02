import React, { useEffect, useState } from "react";
import { Button, List, Upload, message, Popconfirm, Select, Card, Tooltip } from "antd";
import { UploadOutlined, DeleteOutlined, FileTextOutlined, InfoCircleOutlined } from "@ant-design/icons";
import lectureService, { Course, Lesson, LectureMaterial } from "../../../service/lecture.service";
import { useCache } from "../../../hooks/useCache";

const LecturePage: React.FC = () => {
  const [materials, setMaterials] = useState<LectureMaterial[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [selectedLesson, setSelectedLesson] = useState<number | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedCourse, setSelectedCourse] = useState<number | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]); // Add lessons state
  const { clearCache, getCacheStats } = useCache();

  // Fetch courses and their lessons
  const fetchCourses = async () => {
    try {
      setLoading(true);
      const coursesData = await lectureService.getCourses();
      setCourses(coursesData);
    } catch (error) {
      console.error('Error fetching courses:', error);
      message.error('Failed to fetch courses');
    } finally {
      setLoading(false);
    }
  };

  // Fetch lessons for selected course
  const fetchLessons = async (lessonIds: number[]) => {
    try {
      setLoading(true);
      const lessonsData = await lectureService.getLessonsByIds(lessonIds);
      setLessons(lessonsData);
    } catch (error) {
      console.error('Error fetching lessons:', error);
      message.error('Failed to fetch lessons');
    } finally {
      setLoading(false);
    }
  };

  // Fetch materials for selected lesson
  const fetchMaterials = async (lessonId: number) => {
    try {
      setLoading(true);
      const materials = await lectureService.getMaterialsByLessonId(lessonId);
      setMaterials(materials);
    } catch (error) {
      console.error("Error fetching materials:", error);
      message.error("Failed to fetch lecture materials");
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (file: File) => {
    if (!selectedLesson) {
      message.error('Please select a lesson first');
      return false;
    }

    try {
      await lectureService.uploadMaterial(file, selectedLesson);
      message.success("Lecture uploaded successfully");
      fetchMaterials(selectedLesson);
    } catch (error) {
      console.error("Error uploading lecture:", error);
      message.error("Failed to upload lecture");
    }
  };

  const handleDelete = async (id: number) => {
    if (!selectedLesson) return;
    
    try {
      await lectureService.deleteMaterial(id);
      message.success("Lecture deleted successfully");
      fetchMaterials(selectedLesson);
    } catch (error) {
      console.error("Error deleting lecture:", error);
      message.error("Failed to delete lecture");
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  useEffect(() => {
    if (selectedLesson) {
      fetchMaterials(selectedLesson);
    }
  }, [selectedLesson]);

  const handleCourseChange = (value: number) => {
    setSelectedCourse(value);
    setSelectedLesson(null);
    setMaterials([]);
    
    // Fetch lessons for selected course
    const selectedCourseData = courses.find((c: Course) => c.id === value);
    if (selectedCourseData?.lessonIds) {
      fetchLessons(selectedCourseData.lessonIds);
    } else {
      setLessons([]);
    }
  };

  const handleLessonChange = (value: number) => {
    setSelectedLesson(value);
  };

  console.log('Available courses:', courses);
  console.log('Available lessons:', lessons);
  console.log('Selected course ID:', selectedCourse);
  console.log('Selected lesson ID:', selectedLesson);
  console.log('Cache stats:', getCacheStats());
  
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
                    const stats = getCacheStats();
                    message.info(`Cache contains ${stats.size} items: ${stats.keys.join(', ')}`);
                  }}
                >
                  Cache Info
                </Button>
              </Tooltip>
              
              <Button 
                size="small"
                onClick={() => {
                  clearCache();
                  message.success('Cache cleared successfully');
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

            <Select<number>
              className="w-64"
              placeholder="Select Lesson"
              value={selectedLesson}
              onChange={handleLessonChange}
              disabled={!selectedCourse}
              showSearch
              optionFilterProp="children"
            >
              {lessons.map((lesson: Lesson) => (
                <Select.Option key={lesson.id} value={lesson.id}>
                  {lesson.name || `Lesson ${lesson.id}`}
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
              disabled={!selectedLesson}
            >
              <Button 
                icon={<UploadOutlined />}
                disabled={!selectedLesson}
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
          locale={{ emptyText: selectedLesson ? 'No lectures found' : 'Please select a lesson' }}
          renderItem={(item: LectureMaterial) => (
            <List.Item key={item.id}>
              <Card
                hoverable
                className="shadow-sm"
                actions={[
                  <Popconfirm
                    key="delete"
                    title="Are you sure you want to delete this lecture?"
                    onConfirm={() => handleDelete(item.id)}
                    okText="Yes"
                    cancelText="No"
                  >
                    <DeleteOutlined />
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
      </div>
    </div>
  );
};

export default LecturePage;