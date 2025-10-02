import ApiService from './api.service';

export interface Course {
  id: number;
  name: string; // Backend trả về name thay vì title
  lessonIds: number[]; // Backend trả về lessonIds thay vì lessons
}

export interface Lesson {
  id: number;
  name: string; // Backend trả về name thay vì title
}

export interface LectureMaterial {
  id: number;
  materLink: string;
  materType: string;
  uploadedAt: string;
  lessonId: number;
}

export interface ServerResponse<T> {
  statusCode: number;
  message: string;
  data: T;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

class LectureService {
  protected apiService = ApiService();

  async getCourses(): Promise<Course[]> {
    try {
      const response = await this.apiService.get<ServerResponse<PaginatedResponse<Course>>>('/api/v1/courses');
      const data = response.data;
      return data.content || [];
    } catch (error) {
      console.error('Error fetching courses:', error);
      throw error;
    }
  }

  async getLessonsByIds(lessonIds: number[]): Promise<Lesson[]> {
    try {
      if (!lessonIds || lessonIds.length === 0) {
        return [];
      }
      
      // Fetch each lesson individually
      const lessonsPromises = lessonIds.map(async (id) => {
        try {
          const response = await this.apiService.get<ServerResponse<Lesson>>(
            `/api/v1/lessons/${id}`
          );
          return response.data;
        } catch (error) {
          console.error(`Error fetching lesson ${id}:`, error);
          return null;
        }
      });
      
      const lessons = await Promise.all(lessonsPromises);
      const validLessons = lessons.filter(lesson => lesson !== null) as Lesson[];
      
      console.log('Fetched lessons:', validLessons);
      return validLessons;
    } catch (error) {
      console.error('Error fetching lessons by IDs:', error);
      return [];
    }
  }

  async getMaterialsByLessonId(lessonId: number): Promise<LectureMaterial[]> {
    try {
      const response = await this.apiService.get<ServerResponse<PaginatedResponse<LectureMaterial>>>(
        `/api/v1/material/lessons/${lessonId}`
      );

      console.log('Fetched materials response:', response);
      return response.data.content || [];
    } catch (error) {
      console.error('Error fetching materials:', error);
      throw error;
    }
  }

  async uploadMaterial(file: File, lessonId: number): Promise<LectureMaterial> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('folder', 'lectures');
      formData.append('lessonId', lessonId.toString());
      formData.append('materType', 'text/markdown');

      const response = await this.apiService.post<ServerResponse<LectureMaterial>>(
        '/api/v1/material/upload/lesson',
        formData
      );
      return response.data;
    } catch (error) {
      console.error('Error uploading material:', error);
      throw error;
    }
  }

  async deleteMaterial(id: number): Promise<void> {
    try {
      await this.apiService.delete(`/api/v1/material/${id}`);
    } catch (error) {
      console.error('Error deleting material:', error);
      throw error;
    }
  }

  // Helper method to get file name from material link
  getFileName(materLink: string): string {
    return materLink.split('/').pop() || 'Unknown File';
  }
}

export const lectureService = new LectureService();
export default lectureService;