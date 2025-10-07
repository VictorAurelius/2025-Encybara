import ApiService from './api.service';
import { globalCache } from './cache.service';

export interface Question {
  id: number;
  quesContent: string;
  skillType: string;
}

export interface Lesson {
  id: number;
  name: string;
  skillType: string;
  questionIds: number[];
}

export interface LessonFilters {
  skillType?: string;
  courseId?: number;
}

export interface ServerResponse<T> {
  statusCode: number;
  message: string;
  data: T;
}

class LessonService {
  protected apiService = ApiService();

  // Generate cache key
  private generateCacheKey(prefix: string, params?: any): string {
    if (!params) return prefix;
    return `${prefix}_${JSON.stringify(params)}`;
  }

  async getAllLessons(filters: LessonFilters = {}): Promise<Lesson[]> {
    const cacheKey = this.generateCacheKey('all_lessons', filters);
    
    const cached = globalCache.get<Lesson[]>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const queryParams = new URLSearchParams({
        page: '1',
        size: '1000'
      });

      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, value.toString());
        }
      });

      const response = await this.apiService.get<ServerResponse<{ content: Lesson[] }>>(
        `/api/v1/lessons?${queryParams}`
      );
      
      const lessons = response.data.content || [];
      
      // Cache for 10 minutes
      globalCache.set(cacheKey, lessons, 10 * 60 * 1000);
      
      return lessons;
    } catch (error) {
      console.error('Error fetching all lessons:', error);
      throw error;
    }
  }

  async getQuestionsBySkillType(skillType: string): Promise<Question[]> {
    const cacheKey = `questions_skill_${skillType}`;
    
    const cached = globalCache.get<Question[]>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.apiService.get<ServerResponse<{ content: Question[] }>>(
        `/api/v1/questions?page=1&size=1000`
      );
      
      const allQuestions = response.data.content || [];
      const filteredQuestions = allQuestions.filter(q => q.skillType === skillType);
      
      globalCache.set(cacheKey, filteredQuestions, 15 * 60 * 1000);
      
      return filteredQuestions;
    } catch (error) {
      console.error(`Error fetching questions for skill ${skillType}:`, error);
      throw error;
    }
  }

  async createLesson(lessonData: Partial<Lesson>): Promise<Lesson> {
    try {
      const response = await this.apiService.post<ServerResponse<Lesson>>(
        '/api/v1/lessons',
        lessonData
      );

      this.invalidateLessonsCache();
      
      return response.data;
    } catch (error) {
      console.error('Error creating lesson:', error);
      throw error;
    }
  }

  async updateLesson(lessonId: number, lessonData: Partial<Lesson>): Promise<Lesson> {
    try {
      const response = await this.apiService.put<ServerResponse<Lesson>>(
        `/api/v1/lessons/${lessonId}`,
        lessonData
      );

      this.invalidateLessonsCache();
      
      return response.data;
    } catch (error) {
      console.error(`Error updating lesson ${lessonId}:`, error);
      throw error;
    }
  }

  async deleteLesson(lessonId: number): Promise<void> {
    try {
      await this.apiService.delete(`/api/v1/lessons/${lessonId}`);
      this.invalidateLessonsCache();  
    } catch (error) {
      console.error(`Error deleting lesson ${lessonId}:`, error);
      throw error;
    }
  }

  async addQuestionsToLesson(lessonId: number, questionIds: number[]): Promise<void> {
    try {
      await this.apiService.post(
        `/api/v1/lessons/${lessonId}/questions`,
        { questionIds }
      );

      this.invalidateLessonsCache();
      
    } catch (error) {
      console.error(`Error adding questions to lesson ${lessonId}:`, error);
      throw error;
    }
  }

  async removeQuestionFromLesson(lessonId: number, questionId: number): Promise<void> {
    try {
      await this.apiService.delete(
        `/api/v1/lessons/${lessonId}/questions`,
        { questionId }
      );

      this.invalidateLessonsCache();
      
    } catch (error) {
      console.error(`Error removing question from lesson ${lessonId}:`, error);
      throw error;
    }
  }

  async getCourseDetails(courseId: number): Promise<{ lessonIds: number[] }> {
    const cacheKey = `course_details_${courseId}`;
    
    const cached = globalCache.get<{ lessonIds: number[] }>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.apiService.get<ServerResponse<{ lessonIds: number[] }>>(
        `/api/v1/courses/${courseId}`
      );
      
      const courseDetails = response.data;
      
      globalCache.set(cacheKey, courseDetails, 5 * 60 * 1000);
      
      return courseDetails;
    } catch (error) {
      console.error(`Error fetching course ${courseId} details:`, error);
      throw error;
    }
  }

  async addLessonsToCourse(courseId: number, lessonIds: number[]): Promise<void> {
    try {
      await this.apiService.post(
        `/api/v1/courses/${courseId}/lessons`,
        { lessonIds }
      );

      // Invalidate course details cache
      globalCache.delete(`course_details_${courseId}`);
      
    } catch (error) {
      console.error(`Error adding lessons to course ${courseId}:`, error);
      throw error;
    }
  }

  async removeLessonFromCourse(courseId: number, lessonId: number): Promise<void> {
    try {
      await this.apiService.delete(
        `/api/v1/courses/${courseId}/lessons`,
        { lessonId }
      );

      // Invalidate course details cache
      globalCache.delete(`course_details_${courseId}`);
      
    } catch (error) {
      console.error(`Error removing lesson from course ${courseId}:`, error);
      throw error;
    }
  }

  // Cache management methods
  invalidateLessonsCache(): void {
    globalCache.invalidatePattern('all_lessons_.*');
  }

  invalidateQuestionsCache(skillType?: string): void {
    if (skillType) {
      globalCache.delete(`questions_skill_${skillType}`);
    } else {
      globalCache.invalidatePattern('questions_skill_.*');
    }
  }

  clearAllLessonCache(): void {
    globalCache.invalidatePattern('all_lessons_.*');
    globalCache.invalidatePattern('questions_skill_.*');
    globalCache.invalidatePattern('course_details_.*');
  }

  getLessonCacheStats(): { size: number; keys: string[] } {
    const stats = globalCache.getStats();
    const lessonKeys = stats.keys.filter(key => 
      key.startsWith('all_lessons_') || 
      key.startsWith('questions_skill_') ||
      key.startsWith('course_details_')
    );
    
    return {
      size: lessonKeys.length,
      keys: lessonKeys
    };
  }
}

export const lessonService = new LessonService();
export default lessonService;