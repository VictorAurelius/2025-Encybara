import { useCallback } from 'react';
import { globalCache } from '../service/cache.service';

export const useCache = () => {
  const clearCache = useCallback((pattern?: string) => {
    if (pattern) {
      globalCache.invalidatePattern(pattern);
      console.log(`🗑️ Cleared cache with pattern: ${pattern}`);
    } else {
      globalCache.clear();
      console.log('🗑️ Cleared all cache');
    }
  }, []);

  const getCacheStats = useCallback(() => {
    return globalCache.getStats();
  }, []);

  const getCachedData = useCallback(<T>(key: string): T | null => {
    return globalCache.get<T>(key);
  }, []);

  const setCachedData = useCallback(<T>(key: string, data: T, expiry?: number) => {
    globalCache.set(key, data, expiry);
  }, []);

  const deleteCachedData = useCallback((key: string) => {
    globalCache.delete(key);
  }, []);

  return {
    clearCache,
    getCacheStats,
    getCachedData,
    setCachedData,
    deleteCachedData
  };
};

export default useCache;