import ApiService from './api.service';
import { globalCache } from './cache.service';

export interface IQuestionChoice {
  id?: number;
  choiceContent: string;
  choiceKey: boolean;
}

export interface IQuestion {
  id?: number;
  quesContent: string;
  keyword: string;
  quesType: string;
  skillType: string;
  point: number;
  quesMaterial: string;
  questionChoices: IQuestionChoice[];
}

export interface QuestionFilters {
  quesType?: string;
  skillType?: string;
  keyword?: string;
  point?: number;
}

export interface PaginationParams {
  page: number;
  size: number;
}

export interface QuestionsResponse {
  content: IQuestion[];
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

class QuestionService {
  protected apiService = ApiService();

  // Generate cache key
  private generateCacheKey(prefix: string, params?: any): string {
    if (!params) return prefix;
    return `${prefix}_${JSON.stringify(params)}`;
  }

  async getQuestions(
    filters: QuestionFilters = {},
    pagination: PaginationParams = { page: 1, size: 10 }
  ): Promise<QuestionsResponse> {
    const cacheKey = this.generateCacheKey('questions', { filters, pagination });
    
    const cached = globalCache.get<QuestionsResponse>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const queryParams = new URLSearchParams({
        page: pagination.page.toString(),
        size: pagination.size.toString(),
        point: (filters.point || 10).toString()
      });

      // Add filters to query params
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '' && key !== 'point') {
          queryParams.append(key, value.toString());
        }
      });

      const response = await this.apiService.get<ServerResponse<QuestionsResponse>>(
        `/api/v1/questions?${queryParams}`
      );
      
      const questionsData = response.data;
      
      // Cache for 8 minutes (questions change moderately)
      globalCache.set(cacheKey, questionsData, 8 * 60 * 1000);
      
      return questionsData;
    } catch (error) {
      console.error('Error fetching questions:', error);
      throw error;
    }
  }

  async getAllLessons(): Promise<any[]> {
    const cacheKey = 'all_lessons_list';
    
    const cached = globalCache.get<any[]>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.apiService.get<ServerResponse<{ content: any[] }>>(
        '/api/v1/lessons'
      );
      
      const lessons = response.data.content || [];
      
      // Cache for 15 minutes (lessons don't change often)
      globalCache.set(cacheKey, lessons, 15 * 60 * 1000);
      
      return lessons;
    } catch (error) {
      console.error('Error fetching lessons:', error);
      throw error;
    }
  }

  async getQuestionLessonMap(): Promise<{ [key: number]: Array<{ id: number, name: string }> }> {
    const cacheKey = 'question_lesson_map';
    
    const cached = globalCache.get<{ [key: number]: Array<{ id: number, name: string }> }>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const lessons = await this.getAllLessons();
      
      const questionLessonMap: { [key: number]: Array<{ id: number, name: string }> } = {};
      
      lessons.forEach((lesson: any) => {
        if (lesson.questionIds && Array.isArray(lesson.questionIds)) {
          lesson.questionIds.forEach((qId: number) => {
            if (!questionLessonMap[qId]) {
              questionLessonMap[qId] = [];
            }
            questionLessonMap[qId].push({
              id: lesson.id,
              name: lesson.name
            });
          });
        }
      });
      
      // Cache for 10 minutes (lesson assignments change occasionally)
      globalCache.set(cacheKey, questionLessonMap, 10 * 60 * 1000);
      
      return questionLessonMap;
    } catch (error) {
      console.error('Error building question-lesson map:', error);
      throw error;
    }
  }

  async createQuestion(questionData: IQuestion): Promise<IQuestion> {
    try {
      const response = await this.apiService.post<ServerResponse<IQuestion>>(
        '/api/v1/questions',
        questionData
      );

      // Invalidate questions cache since new question created
      this.invalidateQuestionsCache();
      
      return response.data;
    } catch (error) {
      console.error('Error creating question:', error);
      throw error;
    }
  }

  async updateQuestion(questionId: number, questionData: IQuestion): Promise<IQuestion> {
    try {
      const response = await this.apiService.put<ServerResponse<IQuestion>>(
        `/api/v1/questions/${questionId}`,
        questionData
      );

      // Invalidate questions cache since question updated
      this.invalidateQuestionsCache();
      
      return response.data;
    } catch (error) {
      console.error(`Error updating question ${questionId}:`, error);
      throw error;
    }
  }

  async deleteQuestion(questionId: number): Promise<void> {
    try {
      await this.apiService.delete(`/api/v1/questions/${questionId}`);

      // Invalidate related caches
      this.invalidateQuestionsCache();
      this.invalidateQuestionLessonMap();
      
    } catch (error) {
      console.error(`Error deleting question ${questionId}:`, error);
      throw error;
    }
  }

  async deleteMultipleQuestions(questionIds: number[]): Promise<void> {
    try {
      await this.apiService.delete('/api/v1/questions/batch', { ids: questionIds });

      // Invalidate related caches
      this.invalidateQuestionsCache();
      this.invalidateQuestionLessonMap();
      
    } catch (error) {
      console.error('Error deleting multiple questions:', error);
      throw error;
    }
  }

  async uploadMaterial(file: File): Promise<{ url: string }> {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await this.apiService.post<ServerResponse<{ url: string }>>(
        '/api/v1/upload/material',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      return response.data;
    } catch (error) {
      console.error('Error uploading material:', error);
      throw error;
    }
  }

  // Cache management methods
  invalidateQuestionsCache(): void {
    globalCache.invalidatePattern('questions_.*');
  }

  invalidateQuestionLessonMap(): void {
    globalCache.delete('question_lesson_map');
  }

  invalidateLessonsCache(): void {
    globalCache.delete('all_lessons_list');
  }

  clearAllQuestionCache(): void {
    globalCache.invalidatePattern('questions_.*');
    globalCache.delete('question_lesson_map');
    globalCache.delete('all_lessons_list');
  }

  getQuestionCacheStats(): { size: number; keys: string[] } {
    const stats = globalCache.getStats();
    const questionKeys = stats.keys.filter(key => 
      key.startsWith('questions_') || 
      key === 'question_lesson_map' ||
      key === 'all_lessons_list'
    );
    
    return {
      size: questionKeys.length,
      keys: questionKeys
    };
  }
}

export const questionService = new QuestionService();
export default questionService;