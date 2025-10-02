import ApiService from './api.service';
import { globalCache } from './cache.service';

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
    const cacheKey = 'courses';
    
    // Check cache first
    const cachedCourses = globalCache.get<Course[]>(cacheKey);
    if (cachedCourses) {
      console.log('📦 Using cached courses data');
      return cachedCourses;
    }

    try {
      console.log('🌐 Fetching courses from API');
      const response = await this.apiService.get<ServerResponse<PaginatedResponse<Course>>>('/api/v1/courses');
      const data = response.data;
      const courses = data.content || [];
      
      // Cache the result for 10 minutes
      globalCache.set(cacheKey, courses, 10 * 60 * 1000);
      
      return courses;
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
      
      const lessons: Lesson[] = [];
      const uncachedIds: number[] = [];
      
      // Check cache for each lesson
      for (const id of lessonIds) {
        const cacheKey = `lesson_${id}`;
        const cachedLesson = globalCache.get<Lesson>(cacheKey);
        
        if (cachedLesson) {
          lessons.push(cachedLesson);
        } else {
          uncachedIds.push(id);
        }
      }
      
      console.log(`📦 Found ${lessons.length} cached lessons, fetching ${uncachedIds.length} from API`);
      
      // Fetch uncached lessons
      if (uncachedIds.length > 0) {
        const lessonsPromises = uncachedIds.map(async (id) => {
          try {
            const response = await this.apiService.get<ServerResponse<Lesson>>(
              `/api/v1/lessons/${id}`
            );
            const lesson = response.data;
            
            // Cache individual lesson for 15 minutes
            globalCache.set(`lesson_${id}`, lesson, 15 * 60 * 1000);
            
            return lesson;
          } catch (error) {
            console.error(`Error fetching lesson ${id}:`, error);
            return null;
          }
        });
        
        const fetchedLessons = await Promise.all(lessonsPromises);
        const validLessons = fetchedLessons.filter(lesson => lesson !== null) as Lesson[];
        lessons.push(...validLessons);
      }
      
      // Sort lessons by original order
      const sortedLessons = lessonIds
        .map(id => lessons.find(lesson => lesson.id === id))
        .filter(lesson => lesson !== undefined) as Lesson[];
      
      console.log('Fetched lessons:', sortedLessons);
      return sortedLessons;
    } catch (error) {
      console.error('Error fetching lessons by IDs:', error);
      return [];
    }
  }

  async getMaterialsByLessonId(lessonId: number): Promise<LectureMaterial[]> {
    const cacheKey = `materials_lesson_${lessonId}`;
    
    // Check cache first
    const cachedMaterials = globalCache.get<LectureMaterial[]>(cacheKey);
    if (cachedMaterials) {
      console.log(`📦 Using cached materials for lesson ${lessonId}`);
      return cachedMaterials;
    }

    try {
      console.log(`🌐 Fetching materials for lesson ${lessonId} from API`);
      const response = await this.apiService.get<ServerResponse<PaginatedResponse<LectureMaterial>>>(
        `/api/v1/material/lessons/${lessonId}`
      );
      
      const materials = response.data.content || [];
      
      // Cache materials for 5 minutes (shorter since they change more frequently)
      globalCache.set(cacheKey, materials, 5 * 60 * 1000);
      
      console.log('Fetched materials response:', response);
      return materials;
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
      
      // Invalidate materials cache for this lesson
      globalCache.delete(`materials_lesson_${lessonId}`);
      console.log(`🗑️ Invalidated materials cache for lesson ${lessonId}`);
      
      return response.data;
    } catch (error) {
      console.error('Error uploading material:', error);
      throw error;
    }
  }

  async deleteMaterial(id: number): Promise<void> {
    try {
      await this.apiService.delete(`/api/v1/material/${id}`);
      
      // Invalidate all materials cache since we don't know which lesson this material belongs to
      globalCache.invalidatePattern('materials_lesson_.*');
      console.log('🗑️ Invalidated all materials cache');
      
    } catch (error) {
      console.error('Error deleting material:', error);
      throw error;
    }
  }

  // Helper method to get file name from material link
  getFileName(materLink: string): string {
    return materLink.split('/').pop() || 'Unknown File';
  }

  // Cache management methods
  clearAllCache(): void {
    globalCache.clear();
    console.log('🗑️ Cleared all lecture cache');
  }

  clearCoursesCache(): void {
    globalCache.delete('courses');
    console.log('🗑️ Cleared courses cache');
  }

  clearLessonsCache(): void {
    globalCache.invalidatePattern('lesson_.*');
    console.log('🗑️ Cleared lessons cache');
  }

  clearMaterialsCache(): void {
    globalCache.invalidatePattern('materials_lesson_.*');
    console.log('🗑️ Cleared materials cache');
  }

  getCacheStats(): { size: number; keys: string[] } {
    return globalCache.getStats();
  }
}

export const lectureService = new LectureService();
export default lectureService;