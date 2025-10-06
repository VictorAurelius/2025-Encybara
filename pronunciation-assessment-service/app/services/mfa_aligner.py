"""Montreal Forced Aligner integration with optimizations."""

import os
import tempfile
import subprocess
import logging
import shutil
from typing import Optional

logger = logging.getLogger(__name__)


class MontrealForcedAligner:
    """Montreal Forced Aligner integration for forced alignment with speed optimizations."""

    def __init__(self, acoustic_model: str = "english_us_arpa",
                 dictionary: str = "english_us_arpa"):
        self.acoustic_model = acoustic_model
        self.dictionary = dictionary
        self.temp_dir = tempfile.mkdtemp()

    def align_audio_text(self, audio_path: str, transcript: str) -> Optional[str]:
        """
        Perform forced alignment using MFA with optimizations for speed.

        Args:
            audio_path: Path to audio file
            transcript: Text transcript

        Returns:
            Path to generated TextGrid file or None if alignment fails
        """
        try:
            # Create temporary directory structure for MFA
            corpus_dir = os.path.join(self.temp_dir, "corpus")
            output_dir = os.path.join(self.temp_dir, "output")
            os.makedirs(corpus_dir, exist_ok=True)
            os.makedirs(output_dir, exist_ok=True)

            # Copy audio file and create transcript file
            audio_filename = os.path.basename(audio_path)
            base_name = os.path.splitext(audio_filename)[0]

            corpus_audio_path = os.path.join(corpus_dir, audio_filename)
            transcript_path = os.path.join(corpus_dir, f"{base_name}.txt")

            # Copy audio file
            subprocess.run(['cp', audio_path, corpus_audio_path], check=True)

            # Write transcript file
            with open(transcript_path, 'w') as f:
                f.write(transcript)

            # Run MFA alignment with performance optimizations
            mfa_command = [
                'mfa', 'align',
                corpus_dir,
                self.dictionary,
                self.acoustic_model,
                output_dir,
                '--clean',
                # Performance optimizations
                '--single_speaker',  # Single speaker mode for faster processing
                '--no_debug',  # Disable debug output
                '--beam', '10',  # Reduce beam width for faster search (default is 13)
                '--retry_beam', '40',  # Reduce retry beam (default is 100)
                '--num_jobs', '1',  # Use 1 job for small files (avoid overhead)
                '--use_mp',  # Use multiprocessing where possible
            ]

            logger.info(f"Running MFA command: {' '.join(mfa_command)}")
            result = subprocess.run(
                mfa_command,
                capture_output=True,
                text=True,
                timeout=60  # Reduce timeout to 60 seconds (was 300)
            )

            if result.returncode != 0:
                logger.error(f"MFA alignment failed: {result.stderr}")
                return None

            # Find generated TextGrid file
            textgrid_path = os.path.join(output_dir, f"{base_name}.TextGrid")
            if os.path.exists(textgrid_path):
                logger.info(f"Alignment successful: {textgrid_path}")
                return textgrid_path
            else:
                logger.error("TextGrid file not generated")
                return None

        except subprocess.TimeoutExpired:
            logger.error("MFA alignment timed out after 60 seconds")
            return None
        except Exception as e:
            logger.error(f"MFA alignment error: {str(e)}")
            return None

    def cleanup(self):
        """Clean up temporary files."""
        try:
            shutil.rmtree(self.temp_dir)
            logger.debug(f"Cleaned up MFA temp directory: {self.temp_dir}")
        except Exception as e:
            logger.warning(f"Failed to cleanup MFA temp directory: {str(e)}")
