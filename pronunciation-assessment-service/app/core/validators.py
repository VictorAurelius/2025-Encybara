"""Audio and text validation utilities."""

import logging
import shutil

logger = logging.getLogger(__name__)


class AudioValidator:
    """Audio file validation and preprocessing utilities."""

    ALLOWED_EXTENSIONS = {'wav', 'mp3', 'flac', 'm4a'}
    MAX_FILE_SIZE = 6 * 1024 * 1024  # 6MB
    TARGET_SAMPLE_RATE = 8000  # Reduced from 16000 for faster MFA processing

    @staticmethod
    def allowed_file(filename: str) -> bool:
        """Check if file extension is allowed."""
        return '.' in filename and \
               filename.rsplit('.', 1)[1].lower() in AudioValidator.ALLOWED_EXTENSIONS

    @staticmethod
    def validate_file_size(file_obj) -> bool:
        """Validate file size."""
        file_obj.seek(0, 2)  # Seek to end
        size = file_obj.tell()
        file_obj.seek(0)  # Reset to beginning
        return size <= AudioValidator.MAX_FILE_SIZE

    @staticmethod
    def preprocess_audio(input_path: str, output_path: str) -> bool:
        """
        Simple audio preprocessing - just copy file for OpenAI Whisper.

        Args:
            input_path: Path to input audio file
            output_path: Path to output processed file

        Returns:
            True if processing successful, False otherwise
        """
        try:
            # OpenAI Whisper can handle various formats directly
            # Just copy the file
            shutil.copy2(input_path, output_path)
            logger.info(f"Audio file copied successfully: {output_path}")
            return True

        except Exception as e:
            logger.error(f"Audio preprocessing failed: {str(e)}")
            return False


class TextProcessor:
    """Text preprocessing utilities."""

    @staticmethod
    def clean_transcript(transcript: str) -> str:
        """
        Clean and normalize transcript text.

        Args:
            transcript: Raw transcript text

        Returns:
            Cleaned transcript text
        """
        if not transcript:
            return ""

        # Remove extra whitespace and normalize
        cleaned = ' '.join(transcript.strip().split())

        # Remove special characters that might cause issues with MFA
        cleaned = ''.join(char for char in cleaned if char.isalnum() or char.isspace())

        return cleaned.lower()

    @staticmethod
    def validate_transcript(transcript: str) -> bool:
        """Validate transcript content."""
        if not transcript or len(transcript.strip()) == 0:
            return False

        # Check for minimum length
        words = transcript.strip().split()
        if len(words) < 1:
            return False

        # Check for maximum length (prevent abuse)
        if len(transcript) > 1000:
            return False

        return True
