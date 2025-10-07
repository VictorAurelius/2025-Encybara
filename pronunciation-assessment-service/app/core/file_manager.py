"""File management utilities."""

import os
import tempfile
import logging
from werkzeug.utils import secure_filename

logger = logging.getLogger(__name__)


class FileManager:
    """File management utilities."""

    @staticmethod
    def create_temp_file(suffix: str = '.wav') -> str:
        """Create temporary file and return path."""
        temp_fd, temp_path = tempfile.mkstemp(suffix=suffix)
        os.close(temp_fd)  # Close the file descriptor
        return temp_path

    @staticmethod
    def cleanup_files(*file_paths: str) -> None:
        """Clean up temporary files."""
        for file_path in file_paths:
            try:
                if os.path.exists(file_path):
                    os.unlink(file_path)
                    logger.debug(f"Cleaned up file: {file_path}")
            except Exception as e:
                logger.warning(f"Failed to cleanup file {file_path}: {str(e)}")

    @staticmethod
    def secure_filename_with_timestamp(filename: str) -> str:
        """Generate secure filename with timestamp."""
        import time
        secure_name = secure_filename(filename)
        timestamp = str(int(time.time()))
        name, ext = os.path.splitext(secure_name)
        return f"{name}_{timestamp}{ext}"
