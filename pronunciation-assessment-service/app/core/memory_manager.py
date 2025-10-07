"""Memory management utilities."""

import gc
import logging

logger = logging.getLogger(__name__)


class MemoryManager:
    """Memory optimization utilities."""

    @staticmethod
    def get_memory_usage() -> float:
        """Get current memory usage in MB."""
        import psutil
        process = psutil.Process()
        return process.memory_info().rss / 1024 / 1024

    @staticmethod
    def force_garbage_collection():
        """Force garbage collection to free memory."""
        gc.collect()
        logger.debug("Forced garbage collection completed")

    @staticmethod
    def check_memory_limit(limit_mb: float = 3000) -> bool:
        """Check if memory usage is within limit."""
        current_usage = MemoryManager.get_memory_usage()
        if current_usage > limit_mb:
            logger.warning(f"Memory usage ({current_usage:.2f}MB) exceeds limit ({limit_mb}MB)")
            return False
        return True
