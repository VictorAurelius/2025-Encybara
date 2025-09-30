import os
import tempfile
import logging
import librosa
import soundfile as sf
import numpy as np
from typing import Tuple, Optional
from werkzeug.utils import secure_filename

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class AudioValidator:
    """Audio file validation and preprocessing utilities."""
    
    ALLOWED_EXTENSIONS = {'wav', 'mp3', 'flac', 'm4a'}
    MAX_FILE_SIZE = 6 * 1024 * 1024  # 6MB
    TARGET_SAMPLE_RATE = 16000
    
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
        Preprocess audio file to 16kHz mono WAV format.
        
        Args:
            input_path: Path to input audio file
            output_path: Path to output processed file
            
        Returns:
            True if processing successful, False otherwise
        """
        try:
            # Load audio file
            audio, sample_rate = librosa.load(input_path, sr=None, mono=True)
            
            # Resample to target sample rate if needed
            if sample_rate != AudioValidator.TARGET_SAMPLE_RATE:
                audio = librosa.resample(
                    audio, 
                    orig_sr=sample_rate, 
                    target_sr=AudioValidator.TARGET_SAMPLE_RATE
                )
            
            # Normalize audio
            audio = librosa.util.normalize(audio)
            
            # Save as WAV
            sf.write(output_path, audio, AudioValidator.TARGET_SAMPLE_RATE)
            
            logger.info(f"Audio preprocessed successfully: {output_path}")
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
        import gc
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

class ResponseFormatter:
    """API response formatting utilities."""
    
    @staticmethod
    def success_response(data: dict, message: str = "Success") -> dict:
        """Format successful API response."""
        return {
            "success": True,
            "message": message,
            "data": data
        }
    
    @staticmethod
    def error_response(error: str, code: int = 400) -> tuple:
        """Format error API response."""
        return {
            "success": False,
            "error": error,
            "code": code
        }, code
    
    @staticmethod
    def format_assessment_result(
        overall_score: float,
        fluency_score: float,
        phoneme_scores: list,
        total_phonemes: int,
        average_duration: float
    ) -> dict:
        """Format pronunciation assessment result."""
        return {
            "overall_score": round(overall_score, 2),
            "fluency_score": round(fluency_score, 2),
            "phoneme_scores": phoneme_scores,
            "total_phonemes": total_phonemes,
            "average_duration": round(average_duration, 3)
        }