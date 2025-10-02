import ApiService from './api.service';
import { globalCache } from './cache.service';

export interface Course {
  id: number;
  name: string;
  intro: string;
  diffLevel: number;
  recomLevel: number;
  courseType: string;
  speciField: string;
  courseStatus: string;
  group: string;
}

export interface CourseFilters {
  diffLevel?: string;
  courseType?: string;
  group?: string;
  courseStatus?: string;
  keyword?: string;
}

export interface PaginationParams {
  page: number;
  size: number;
}

export interface CoursesResponse {
  content: Course[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export interface ServerResponse<T> {
  statusCode: number;
  message: string;
  data: T;
}

class ProfileService {
  protected apiService = ApiService();

  // Generate cache key based on filters and pagination
  private generateCoursesKey(filters: CourseFilters, pagination: PaginationParams): string {
    const filterStr = JSON.stringify(filters);
    const paginationStr = JSON.stringify(pagination);
    return `courses_${btoa(filterStr + paginationStr)}`;
  }

  async getCourses(
    filters: CourseFilters = {},
    pagination: PaginationParams = { page: 1, size: 10 }
  ): Promise<CoursesResponse> {
    const cacheKey = this.generateCoursesKey(filters, pagination);
    const cachedData = globalCache.get<CoursesResponse>(cacheKey);
    if (cachedData) {
      console.log('📦 Using cached courses data for:', { filters, pagination });
      return cachedData;
    }

    try {      
      const queryParams = new URLSearchParams({
        page: pagination.page.toString(),
        size: pagination.size.toString()
      });

      // Add filters to query params
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          queryParams.append(key, value);
        }
      });

      const response = await this.apiService.get<ServerResponse<CoursesResponse>>(
        `/api/v1/courses?${queryParams}`
      );
      
      const coursesData = response.data;
      
      // Cache for 5 minutes
      globalCache.set(cacheKey, coursesData, 5 * 60 * 1000);
      
      return coursesData;
    } catch (error) {
      console.error('Error fetching courses:', error);
      throw error;
    }
  }

  async getCourseGroups(): Promise<string[]> {
    const cacheKey = 'course_groups';
    
    // Check cache first
    const cachedGroups = globalCache.get<string[]>(cacheKey);
    if (cachedGroups) {
      console.log('📦 Using cached course groups');
      return cachedGroups;
    }

    try {
      console.log('🌐 Fetching course groups from API');
      const response = await this.apiService.get<ServerResponse<{ content: string[] }>>(
        '/api/v1/courses/groups'
      );
      
      const groups = response.data.content || [];
      
      // Cache for 30 minutes (groups don't change often)
      globalCache.set(cacheKey, groups, 30 * 60 * 1000);
      
      return groups;
    } catch (error) {
      console.error('Error fetching course groups:', error);
      throw error;
    }
  }

  async getLessonsByCourseId(courseId: number): Promise<any[]> {
    const cacheKey = `lessons_course_${courseId}`;
    
    // Check cache first
    const cachedLessons = globalCache.get<any[]>(cacheKey);
    if (cachedLessons) {
      console.log(`📦 Using cached lessons for course ${courseId}`);
      return cachedLessons;
    }

    try {
      console.log(`🌐 Fetching lessons for course ${courseId} from API`);
      const response = await this.apiService.get<ServerResponse<{ content: any[] }>>(
        `/api/v1/lessons?courseId=${courseId}`
      );
      
      const lessons = response.data.content || [];
      
      // Cache for 10 minutes
      globalCache.set(cacheKey, lessons, 10 * 60 * 1000);
      
      return lessons;
    } catch (error) {
      console.error(`Error fetching lessons for course ${courseId}:`, error);
      throw error;
    }
  }

  async toggleCourseStatus(courseId: number, currentStatus: string): Promise<void> {
    try {
      let endpoint: string;
      
      if (currentStatus === "PUBLIC") {
        endpoint = `/api/v1/courses/${courseId}/make-private`;
      } else if (currentStatus === "PENDING") {
        endpoint = `/api/v1/courses/${courseId}/publish`;
      } else {
        endpoint = `/api/v1/courses/${courseId}/make-public`;
      }

      console.log(`🔄 Toggling course ${courseId} status from ${currentStatus}`);
      await this.apiService.put(endpoint);
      
      // Invalidate all courses cache since status changed
      this.invalidateCoursesCache();
      console.log('✅ Course status toggled successfully');
      
    } catch (error) {
      console.error(`Error toggling course status for ${courseId}:`, error);
      throw error;
    }
  }

  async deleteCourse(courseId: number): Promise<void> {
    try {
      console.log(`🗑️ Deleting course ${courseId}`);
      await this.apiService.delete(`/api/v1/courses/${courseId}`);
      
      // Invalidate related caches
      this.invalidateCoursesCache();
      this.clearLessonsCache(courseId);
      console.log('✅ Course deleted successfully');
      
    } catch (error) {
      console.error(`Error deleting course ${courseId}:`, error);
      throw error;
    }
  }

  async getCourseById(courseId: number): Promise<Course> {
    const cacheKey = `course_${courseId}`;
    
    const cached = globalCache.get<Course>(cacheKey);
    if (cached) {
      console.log(`📋 Using cached course ${courseId}`);
      return cached;
    }

    try {
      console.log(`🌐 Fetching course ${courseId} from API`);
      const response = await this.apiService.get<ServerResponse<Course>>(
        `/api/v1/courses/${courseId}`
      );
      
      const course = response.data;
      
      // Cache for 10 minutes
      globalCache.set(cacheKey, course, 10 * 60 * 1000);
      
      return course;
    } catch (error) {
      console.error(`Error fetching course ${courseId}:`, error);
      throw error;
    }
  }

  async createCourse(courseData: Partial<Course>): Promise<Course> {
    try {
      console.log('🆕 Creating new course');
      const response = await this.apiService.post<ServerResponse<Course>>(
        '/api/v1/courses',
        courseData
      );

      // Invalidate courses cache since new course created
      this.invalidateCoursesCache();
      
      return response.data;
    } catch (error) {
      console.error('Error creating course:', error);
      throw error;
    }
  }

  async updateCourse(courseId: number, courseData: Partial<Course>): Promise<Course> {
    try {
      console.log(`🔄 Updating course ${courseId}`);
      const response = await this.apiService.put<ServerResponse<Course>>(
        `/api/v1/courses/${courseId}`,
        courseData
      );

      // Invalidate courses cache and specific course cache
      this.invalidateCoursesCache();
      globalCache.delete(`course_${courseId}`);
      
      return response.data;
    } catch (error) {
      console.error(`Error updating course ${courseId}:`, error);
      throw error;
    }
  }

  // Cache management methods
  invalidateCoursesCache(): void {
    globalCache.invalidatePattern('courses_.*');
    console.log('🗑️ Invalidated courses cache');
  }

  clearLessonsCache(courseId?: number): void {
    if (courseId) {
      globalCache.delete(`lessons_course_${courseId}`);
      console.log(`🗑️ Invalidated lessons cache for course ${courseId}`);
    } else {
      globalCache.invalidatePattern('lessons_course_.*');
      console.log('🗑️ Invalidated all lessons cache');
    }
  }

  invalidateGroupsCache(): void {
    globalCache.delete('course_groups');
    console.log('🗑️ Invalidated course groups cache');
  }

  clearAllProfileCache(): void {
    globalCache.invalidatePattern('courses_.*');
    globalCache.invalidatePattern('lessons_course_.*');
    globalCache.delete('course_groups');
    console.log('🗑️ Cleared all profile cache');
  }

  getProfileCacheStats(): { size: number; keys: string[] } {
    const stats = globalCache.getStats();
    const profileKeys = stats.keys.filter(key => 
      key.startsWith('courses_') || 
      key.startsWith('lessons_course_') || 
      key === 'course_groups'
    );
    
    return {
      size: profileKeys.length,
      keys: profileKeys
    };
  }
}

export const profileService = new ProfileService();
export default profileService;