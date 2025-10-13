import ApiService from './api.service';
import { API_BASE_URL } from './api.config';
import { globalCache } from './cache.service';

// Types/Interfaces
export interface ResSpeakingSampleAnswerDTO {
  id: number;
  answerContent: string;
  description: string;
  difficultyLevel: number;
  estimatedScore: number;
  questionId: number;
  questionContent?: string;
  audioLink?: string;
  createBy: string;
  createAt: string;
  updateBy: string;
  updateAt: string;
}

export interface ReqCreateSpeakingSampleAnswerDTO {
  questionId: number;
  answerContent: string;
  description?: string;
  difficultyLevel?: number; // 1-5, default 3
  estimatedScore?: number; // 0-100
}

export interface ReqUpdateSpeakingSampleAnswerDTO {
  id: number;
  questionId?: number;
  answerContent?: string;
  description?: string;
  difficultyLevel?: number;
  estimatedScore?: number;
}

export interface RestResponse<T> {
  statusCode: number;
  message: string;
  data: T;
}

class SpeakingSampleAnswerService {
  protected apiService = ApiService();
  private baseURL: string = '/api/v1/speaking-sample-answers';

  // Generate cache key
  private generateCacheKey(prefix: string, params?: any): string {
    if (!params) return prefix;
    return `${prefix}_${JSON.stringify(params)}`;
  }

  // Invalidate related caches after mutations
  private invalidateCache(questionId?: number): void {
    if (questionId) {
      globalCache.delete(this.generateCacheKey('sample_answers_by_question', { questionId }));
      globalCache.delete(this.generateCacheKey('question_has_samples', { questionId }));
      globalCache.delete(this.generateCacheKey('sample_answers_count', { questionId }));

      // Invalidate difficulty-specific caches for this question
      for (let level = 1; level <= 5; level++) {
        globalCache.delete(
          this.generateCacheKey('sample_answers_by_difficulty', { questionId, difficultyLevel: level })
        );
      }
    }
  }

  /**
   * Tạo mới speaking sample answer
   */
  async createSpeakingSampleAnswer(
    data: ReqCreateSpeakingSampleAnswerDTO
  ): Promise<RestResponse<ResSpeakingSampleAnswerDTO>> {
    const response = await this.apiService.post<RestResponse<ResSpeakingSampleAnswerDTO>>(
      this.baseURL,
      data
    );

    // Invalidate cache after creating
    this.invalidateCache(data.questionId);

    return response;
  }

  /**
   * Cập nhật speaking sample answer
   */
  async updateSpeakingSampleAnswer(
    data: ReqUpdateSpeakingSampleAnswerDTO
  ): Promise<RestResponse<ResSpeakingSampleAnswerDTO>> {
    const response = await this.apiService.put<RestResponse<ResSpeakingSampleAnswerDTO>>(
      this.baseURL,
      data
    );

    // Invalidate specific cache and related caches
    globalCache.delete(this.generateCacheKey('sample_answer_by_id', { id: data.id }));
    if (data.questionId) {
      this.invalidateCache(data.questionId);
    }

    return response;
  }

  /**
   * Lấy speaking sample answer theo ID
   */
  async getSpeakingSampleAnswerById(
    id: number
  ): Promise<RestResponse<ResSpeakingSampleAnswerDTO>> {
    const cacheKey = this.generateCacheKey('sample_answer_by_id', { id });

    const cached = globalCache.get<RestResponse<ResSpeakingSampleAnswerDTO>>(cacheKey);
    if (cached) {
      return cached;
    }

    const response = await this.apiService.get<RestResponse<ResSpeakingSampleAnswerDTO>>(
      `${this.baseURL}/${id}`
    );

    // Cache for 15 minutes
    globalCache.set(cacheKey, response, 15 * 60 * 1000);

    return response;
  }

  /**
   * Lấy tất cả sample answers theo question ID
   */
  async getSampleAnswersByQuestionId(
    questionId: number
  ): Promise<RestResponse<ResSpeakingSampleAnswerDTO[]>> {
    const cacheKey = this.generateCacheKey('sample_answers_by_question', { questionId });

    const cached = globalCache.get<RestResponse<ResSpeakingSampleAnswerDTO[]>>(cacheKey);
    if (cached) {
      return cached;
    }

    const response = await this.apiService.get<RestResponse<ResSpeakingSampleAnswerDTO[]>>(
      `${this.baseURL}/question/${questionId}`
    );

    // Cache for 10 minutes
    globalCache.set(cacheKey, response, 10 * 60 * 1000);

    return response;
  }

  /**
   * Lấy sample answers theo question ID và difficulty level
   */
  async getSampleAnswersByQuestionIdAndDifficulty(
    questionId: number,
    difficultyLevel: number
  ): Promise<RestResponse<ResSpeakingSampleAnswerDTO[]>> {
    const cacheKey = this.generateCacheKey('sample_answers_by_difficulty', {
      questionId,
      difficultyLevel
    });

    const cached = globalCache.get<RestResponse<ResSpeakingSampleAnswerDTO[]>>(cacheKey);
    if (cached) {
      return cached;
    }

    const response = await this.apiService.get<RestResponse<ResSpeakingSampleAnswerDTO[]>>(
      `${this.baseURL}/question/${questionId}/difficulty/${difficultyLevel}`
    );

    // Cache for 10 minutes
    globalCache.set(cacheKey, response, 10 * 60 * 1000);

    return response;
  }

  /**
   * Xóa speaking sample answer
   */
  async deleteSpeakingSampleAnswer(id: number): Promise<RestResponse<void>> {
    // Get the answer first to know which question to invalidate
    let questionId: number | undefined;
    try {
      const answer = await this.getSpeakingSampleAnswerById(id);
      questionId = answer.data.questionId;
    } catch (error) {
      // Ignore error, just delete
    }

    const response = await this.apiService.delete<RestResponse<void>>(
      `${this.baseURL}/${id}`
    );

    // Invalidate caches
    globalCache.delete(this.generateCacheKey('sample_answer_by_id', { id }));
    if (questionId) {
      this.invalidateCache(questionId);
    }

    return response;
  }

  /**
   * Kiểm tra question có sample answers không
   */
  async checkQuestionHasSampleAnswers(questionId: number): Promise<RestResponse<boolean>> {
    const cacheKey = this.generateCacheKey('question_has_samples', { questionId });

    const cached = globalCache.get<RestResponse<boolean>>(cacheKey);
    if (cached) {
      return cached;
    }

    const response = await this.apiService.get<RestResponse<boolean>>(
      `${this.baseURL}/question/${questionId}/exists`
    );

    // Cache for 10 minutes
    globalCache.set(cacheKey, response, 10 * 60 * 1000);

    return response;
  }

  /**
   * Đếm số lượng sample answers của một question
   */
  async countSampleAnswersByQuestionId(questionId: number): Promise<RestResponse<number>> {
    const cacheKey = this.generateCacheKey('sample_answers_count', { questionId });

    const cached = globalCache.get<RestResponse<number>>(cacheKey);
    if (cached) {
      return cached;
    }

    const response = await this.apiService.get<RestResponse<number>>(
      `${this.baseURL}/question/${questionId}/count`
    );

    // Cache for 10 minutes
    globalCache.set(cacheKey, response, 10 * 60 * 1000);

    return response;
  }

  async uploadAudio(
    sampleId: number,
    file: File
  ): Promise<RestResponse<string>> {
    const form = new FormData();
    form.append('file', file);

    const response = await this.apiService.post<RestResponse<string>>(
      `${this.baseURL}/${sampleId}/upload-audio`,
      form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );

    // Invalidate cache của sample này và các cache theo question
    globalCache.delete(this.generateCacheKey('sample_answer_by_id', { id: sampleId }));
    try {
      const sample = await this.getSpeakingSampleAnswerById(sampleId);
      this.invalidateCache(sample.data.questionId);
    } catch (_) { }
    return response;
  }


  async updateAudioLink(
    id: number,
    audioLink: string
  ): Promise<RestResponse<ResSpeakingSampleAnswerDTO>> {
    const response = await this.apiService.put<RestResponse<ResSpeakingSampleAnswerDTO>>(
      `${this.baseURL}/${id}/audio-link`,
      { id, audioLink }
    );

    // Invalidate caches liên quan
    globalCache.delete(this.generateCacheKey('sample_answer_by_id', { id }));
    try {
      const sample = await this.getSpeakingSampleAnswerById(id);
      this.invalidateCache(sample.data.questionId);
    } catch (_) { }
    return response;
  }

  // Xóa audio của sample
  async deleteAudio(id: number): Promise<RestResponse<void>> {
    const response = await this.apiService.delete<RestResponse<void>>(
      `${this.baseURL}/${id}/audio`
    );
    globalCache.delete(this.generateCacheKey('sample_answer_by_id', { id }));
    try {
      const sample = await this.getSpeakingSampleAnswerById(id);
      this.invalidateCache(sample.data.questionId);
    } catch (_) { }
    return response;
  }

  // Chuẩn hóa link audio để phát được trong <audio>
  getPlayableAudioUrl(rawLink?: string): string | undefined {
    if (!rawLink) return undefined;
    let url = rawLink.trim();

    // Thay thế host nội bộ khi backend trả về 0.0.0.0
    if (API_BASE_URL) {
      url = url.replace('http://0.0.0.0:8080', API_BASE_URL);
    }

    // Nếu là đường dẫn tuyệt đối từ backend (/uploadfile/...)
    if (API_BASE_URL && url.startsWith('/')) {
      url = `${API_BASE_URL}${url}`;
    }

    // Encode khoảng trắng
    url = url.replace(/ /g, '%20');
    return url;
  }
  /**
   * Helper: Lấy text của difficulty level
   */
  getDifficultyLevelText(level: number): string {
    const levels: { [key: number]: string } = {
      1: 'Basic',
      2: 'Elementary',
      3: 'Intermediate',
      4: 'Advanced',
      5: 'Expert',
    };
    return levels[level] || 'Unknown';
  }

  /**
   * Helper: Lấy score range
   */
  getScoreRange(score: number): string {
    if (!score) return 'Not specified';
    if (score >= 90) return 'Excellent (90-100)';
    if (score >= 80) return 'Good (80-89)';
    if (score >= 70) return 'Average (70-79)';
    if (score >= 60) return 'Below Average (60-69)';
    return 'Poor (0-59)';
  }

  /**
   * Clear all cache for this service
   */
  clearAllCache(): void {
    globalCache.clear();
  }
}

// Export singleton instance
export const speakingSampleAnswerService = new SpeakingSampleAnswerService();

// Export class để có thể tạo instance mới nếu cần
export default SpeakingSampleAnswerService;