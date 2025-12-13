import ApiService from './api.service';
import { globalCache } from './cache.service';
import { API_BASE_URL } from './api.config';
import { marked } from 'marked';
export interface Course {
  id: number;
  name: string;
}

export interface LectureMaterial {
  id: number;
  materLink: string;
  materType: string;
  uploadedAt: string;
  courseId?: number;
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

  async getAllCourses(token: string): Promise<Course[]> {
    const cacheKey = 'all_courses';

    const cached = globalCache.get<Course[]>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.apiService.get<ServerResponse<Course[]>>(
        '/api/v1/courses', // This endpoint should return all courses
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const courses = response.data || [];


      // Cache for 10 minutes
      globalCache.set(cacheKey, courses, 10 * 60 * 1000);

      return courses;
    } catch (error) {
      console.error('Error fetching all courses:', error);
      throw error;
    }
  }

  async getCoursesWithMaterials(token: string): Promise<Course[]> {
    const cacheKey = 'courses_with_materials';

    // Check cache first
    const cached = globalCache.get<Course[]>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.apiService.get<ServerResponse<Course[]>>(
        '/api/v1/material/courses-with-materials',
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const courses = response.data || [];

      // Cache for 10 minutes
      globalCache.set(cacheKey, courses, 10 * 60 * 1000);
      return courses;
    } catch (error) {
      console.error('Error fetching courses with materials:', error);
      throw error;
    }
  }

  async getMaterialsByCourseId(courseId: number, token: string): Promise<LectureMaterial[]> {
    const cacheKey = `course_materials_${courseId}`;

    // Check cache first
    const cached = globalCache.get<LectureMaterial[]>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.apiService.get<ServerResponse<LectureMaterial[]>>(
        `/api/v1/material/courses/${courseId}`,
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );

      const materials = response.data || [];

      // Cache for 5 minutes (materials might change frequently)
      globalCache.set(cacheKey, materials, 5 * 60 * 1000);
      return materials;
    } catch (error) {
      console.error(`Error fetching materials for course ${courseId}:`, error);
      throw error;
    }
  }

  async readMarkdownContent(materLink: string): Promise<string> {
    const cacheKey = `markdown_${btoa(materLink)}`;

    const cached = globalCache.get<string>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const processedLink = materLink
        .replace('http://0.0.0.0:8080', 'http://18.136.223.96:8080')  // ← Bỏ :8080
        .replace('http://18.136.223.96:8080', 'http://18.136.223.96:8080')  // ← Bỏ :8080
        .replace(/ /g, '%20');

      console.log("Fetching from:", processedLink);

      // Sử dụng fetch trực tiếp thay vì apiService
      const response = await fetch(processedLink, {
        method: 'GET',
        headers: {
          'Accept': 'text/plain, text/markdown, */*',
          'Content-Type': 'text/plain; charset=utf-8'
        },
        mode: 'cors' // Quan trọng: enable CORS
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch markdown: ${response.statusText}`);
      }

      const rawContent = await response.text();
      globalCache.set(cacheKey, rawContent, 15 * 60 * 1000);

      return rawContent;
    } catch (error) {
      console.error('Error reading markdown content:', error);
      throw error;
    }
  }

  async renderMarkdownToHtml(materLink: string): Promise<string> {
    const cacheKey = `markdown_html_${btoa(materLink)}`;

    const cached = globalCache.get<string>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const rawContent = await this.readMarkdownContent(materLink);

      marked.setOptions({
        breaks: true,
        gfm: true
      });

      // Render markdown to HTML
      const htmlContent = await marked(rawContent);

      // Cache rendered HTML for 20 minutes
      globalCache.set(cacheKey, htmlContent, 20 * 60 * 1000);

      return htmlContent;
    } catch (error) {
      console.error('Error rendering markdown to HTML:', error);
      throw error;
    }
  }

  async uploadMaterial(file: File, courseId: number): Promise<LectureMaterial> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('folder', 'courses');
      formData.append('courseId', courseId.toString());
      formData.append('materType', 'md');

      const response = await this.apiService.post<ServerResponse<LectureMaterial>>(
        '/api/v1/material/upload/course',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      this.invalidateMaterialsCache(courseId);
      return response.data;
    } catch (error) {
      console.error('Error uploading material:', error);
      throw error;
    }
  }

  async deleteMaterial(id: number, courseId: number): Promise<void> {
    try {
      await this.apiService.delete(`/api/v1/material/${id}`);
      this.invalidateMaterialsCache(courseId);

    } catch (error) {
      console.error('Error deleting material:', error);
      throw error;
    }
  }

  // Cache management methods
  invalidateMaterialsCache(courseId?: number): void {
    if (courseId) {
      globalCache.delete(`course_materials_${courseId}`);
    } else {
      globalCache.invalidatePattern('course_materials_.*');
    }

    // Also invalidate courses list
    globalCache.delete('courses_with_materials');
  }

  clearAllLectureCache(): void {
    globalCache.invalidatePattern('course_materials_.*');
    globalCache.invalidatePattern('markdown_.*');
    globalCache.invalidatePattern('markdown_html_.*');
    globalCache.delete('courses_with_materials');
  }

  getLectureCacheStats(): { size: number; keys: string[] } {
    const stats = globalCache.getStats();
    const lectureKeys = stats.keys.filter((key: string) =>
      key.startsWith('course_materials_') ||
      key.startsWith('markdown_') ||
      key.startsWith('markdown_html_') ||
      key === 'courses_with_materials'
    );

    return {
      size: lectureKeys.length,
      keys: lectureKeys
    };
  }

  // Helper method to get file name from material link
  getFileName(materLink: string): string {
    return materLink.split('/').pop() || 'Unknown File';
  }

  // Helper method to process material link (replace base URL)
  processMaterLink(materLink: string): string {
    return materLink.replace('http://0.0.0.0:8080', API_BASE_URL);
  }
}

export const lectureService = new LectureService();
export default lectureService;