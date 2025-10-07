"""Montreal Forced Aligner integration with optimizations."""

import os
import sys
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
        self._check_mfa_installation()

    def _check_mfa_installation(self):
        """Check if MFA is installed and accessible."""
        try:
            # Try to run mfa version command
            result = subprocess.run(
                ['mfa', 'version'],
                capture_output=True,
                text=True,
                timeout=5
            )
            if result.returncode == 0:
                logger.info(f"MFA found: {result.stdout.strip()}")
                return True
            else:
                logger.warning("MFA command found but returned error")
                return False
        except FileNotFoundError:
            error_msg = (
                "Montreal Forced Aligner (MFA) is not installed or not in PATH.\n"
                "This service requires MFA to be installed.\n\n"
                "Installation instructions:\n"
                "1. Docker (Recommended): Use the provided Dockerfile\n"
                "   cd pronunciation-assessment-service && docker-compose up\n\n"
                "2. Conda (Linux/Mac):\n"
                "   conda create -n aligner -c conda-forge montreal-forced-aligner\n"
                "   conda activate aligner\n"
                "   mfa model download acoustic english_us_arpa\n"
                "   mfa model download dictionary english_us_arpa\n\n"
                "3. Windows: MFA is not officially supported on Windows.\n"
                "   Please use Docker or WSL2.\n\n"
                "For more info: https://montreal-forced-aligner.readthedocs.io/"
            )
            logger.error(error_msg)
            raise RuntimeError(error_msg)
        except Exception as e:
            logger.error(f"Error checking MFA installation: {str(e)}")
            return False

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

            # Copy audio file (cross-platform)
            shutil.copy2(audio_path, corpus_audio_path)

            # Write transcript file
            with open(transcript_path, 'w') as f:
                f.write(transcript)

            # Run MFA alignment with aggressive performance optimizations
            mfa_command = [
                'mfa', 'align',
                corpus_dir,
                self.dictionary,
                self.acoustic_model,
                output_dir,
                '--clean',
                # Aggressive performance optimizations for speed
                '--single_speaker',  # Single speaker mode for faster processing
                '--no_debug',  # Disable debug output
                '--beam', '5',  # Very narrow beam for maximum speed (was 10, default 13)
                '--retry_beam', '20',  # Minimal retry beam (was 40, default 100)
                '--max_duration', '30',  # Skip long audio segments
                '--num_jobs', '1',  # Single job to avoid overhead
                '--use_mp',  # Use multiprocessing where possible
                '--no_final_clean',  # Skip final cleanup for speed
                '--acoustic_scale', '0.1',  # Lower acoustic weight for faster convergence
            ]

            logger.info(f"Running MFA command (optimized for <15s): {' '.join(mfa_command)}")

            try:
                result = subprocess.run(
                    mfa_command,
                    capture_output=True,
                    text=True,
                    timeout=30  # Very tight timeout: 30 seconds for max speed
                )

                if result.returncode != 0:
                    logger.error(f"MFA alignment failed with return code {result.returncode}")
                    logger.error(f"STDERR: {result.stderr}")
                    logger.error(f"STDOUT: {result.stdout}")
                    return None
            except FileNotFoundError:
                logger.error("MFA command not found. Please ensure MFA is installed and in PATH.")
                logger.error("For installation instructions, see service documentation.")
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
            logger.error("MFA alignment timed out after 30 seconds")
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
