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
    
    // Check cache first
    const cached = globalCache.get<Course[]>(cacheKey);
    if (cached) {
      console.log('📦 Using cached all courses');
      return cached;
    }

    try {
      console.log('🌐 Fetching all courses from API');
      const response = await this.apiService.get<ServerResponse<Course[]>>(
        '/api/v1/courses', // This endpoint should return all courses
        { headers: { Authorization: `Bearer ${token}` } }
      );
      console.log('Fetched courses:', response.data);
      const courses = response.data || [];
      
      
      // Cache for 10 minutes
      globalCache.set(cacheKey, courses, 10 * 60 * 1000);
      console.log('📦 All courses cached');
      
      return courses;
    } catch (error) {
      console.error('Error fetching all courses:', error);
      throw error;
    }
  }

  async getCoursesWithMaterials(token:string): Promise<Course[]> {
    const cacheKey = 'courses_with_materials';
    
    // Check cache first
    const cached = globalCache.get<Course[]>(cacheKey);
    if (cached) {
      console.log('📦 Using cached courses with materials');
      return cached;
    }

    try {
      console.log('🌐 Fetching courses with materials from API');
      const response = await this.apiService.get<ServerResponse<Course[]>>(
        '/api/v1/material/courses-with-materials',
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      const courses = response.data || [];
      
      // Cache for 10 minutes
      globalCache.set(cacheKey, courses, 10 * 60 * 1000);
      console.log('📦 Courses with materials cached');    
      return courses;
    } catch (error) {
      console.error('Error fetching courses with materials:', error);
      throw error;
    }
  }

  async getMaterialsByCourseId(courseId: number,token: string): Promise<LectureMaterial[]> {
    const cacheKey = `course_materials_${courseId}`;
    
    // Check cache first
    const cached = globalCache.get<LectureMaterial[]>(cacheKey);
    if (cached) {
      console.log(`📦 Using cached materials for course ${courseId}`);
      return cached;
    }

    try {
      console.log(`🌐 Fetching materials for course ${courseId} from API`);
      const response = await this.apiService.get<ServerResponse<LectureMaterial[]>>(
        `/api/v1/material/courses/${courseId}`,
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );

      const materials = response.data || [];
      
      // Cache for 5 minutes (materials might change frequently)
      globalCache.set(cacheKey, materials, 5 * 60 * 1000);
      console.log(`📦 Materials for course ${courseId} cached`);
      
      return materials;
    } catch (error) {
      console.error(`Error fetching materials for course ${courseId}:`, error);
      throw error;
    }
  }

  async readMarkdownContent(materLink: string): Promise<string> {
    const cacheKey = `markdown_${btoa(materLink)}`;
    
    // Check cache first
    const cached = globalCache.get<string>(cacheKey);
    if (cached) {
      console.log('📦 Using cached markdown content');
      return cached;
    }

    try {
      console.log('🌐 Fetching markdown content from:', materLink);
      
      // Replace http://0.0.0.0:8080 with API_BASE_URL and encode spaces
      const processedLink = materLink
        .replace('http://0.0.0.0:8080', API_BASE_URL)
        .replace(/ /g, '%20');
      
      const response = await fetch(processedLink);
      
      if (!response.ok) {
        throw new Error(`Failed to fetch markdown: ${response.statusText}`);
      }
      
      const rawContent = await response.text();
      
      // Cache for 15 minutes (markdown content rarely changes)
      globalCache.set(cacheKey, rawContent, 15 * 60 * 1000);
      console.log('📦 Markdown content cached');
      
      return rawContent;
    } catch (error) {
      console.error('Error reading markdown content:', error);
      throw error;
    }
  }

  async renderMarkdownToHtml(materLink: string): Promise<string> {
    const cacheKey = `markdown_html_${btoa(materLink)}`;
    
    // Check cache first for rendered HTML
    const cached = globalCache.get<string>(cacheKey);
    if (cached) {
      console.log('📦 Using cached rendered markdown HTML');
      return cached;
    }

    try {
      // Get raw markdown content
      const rawContent = await this.readMarkdownContent(materLink);
      
      // Configure marked options for better rendering
      marked.setOptions({
        breaks: true,
        gfm: true
      });
      
      // Render markdown to HTML
      const htmlContent = await marked(rawContent);
      
      // Cache rendered HTML for 20 minutes
      globalCache.set(cacheKey, htmlContent, 20 * 60 * 1000);
      console.log('📦 Rendered markdown HTML cached');
      
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

      console.log(`📤 Uploading material for course ${courseId}`);
      const response = await this.apiService.post<ServerResponse<LectureMaterial>>(
        '/api/v1/material/upload/course',
        formData
      );
      
      // Invalidate related caches
      this.invalidateMaterialsCache(courseId);
      return response.data;
    } catch (error) {
      console.error('Error uploading material:', error);
      throw error;
    }
  }

  async deleteMaterial(id: number, courseId: number): Promise<void> {
    try {
      console.log(`🗑️ Deleting material ${id}`);
      await this.apiService.delete(`/api/v1/material/${id}`);
      // Invalidate related caches
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
      console.log(`🗑️ Invalidated materials cache for course ${courseId}`);
    } else {
      globalCache.invalidatePattern('course_materials_.*');
      console.log('🗑️ Invalidated all course materials cache');
    }
    
    // Also invalidate courses list
    globalCache.delete('courses_with_materials');
  }

  clearAllLectureCache(): void {
    globalCache.invalidatePattern('course_materials_.*');
    globalCache.invalidatePattern('markdown_.*');
    globalCache.invalidatePattern('markdown_html_.*');
    globalCache.delete('courses_with_materials');
    console.log('🗑️ Cleared all lecture cache');
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