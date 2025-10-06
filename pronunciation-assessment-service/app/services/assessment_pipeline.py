"""Complete pronunciation assessment pipeline."""

import logging
from typing import Dict, Optional

from .mfa_aligner import MontrealForcedAligner
from .gop_scorer import GOPScorer
from ..core import AudioValidator, FileManager, MemoryManager

logger = logging.getLogger(__name__)


class PronunciationAssessmentPipeline:
    """Complete pronunciation assessment pipeline with optimizations."""

    def __init__(self, docker_mode: bool = True):
        self.docker_mode = docker_mode
        self.mfa = MontrealForcedAligner()
        self.gop_scorer = GOPScorer()

    def assess_pronunciation(self, audio_path: str, transcript: str) -> Optional[Dict]:
        """
        Complete pronunciation assessment pipeline.

        Args:
            audio_path: Path to audio file
            transcript: Text transcript

        Returns:
            Assessment results or None if failed
        """
        temp_files = []

        try:
            # Memory check
            if not MemoryManager.check_memory_limit():
                logger.warning("Memory usage high, forcing garbage collection")
                MemoryManager.force_garbage_collection()

            # Preprocess audio
            processed_audio_path = FileManager.create_temp_file('.wav')
            temp_files.append(processed_audio_path)

            if not AudioValidator.preprocess_audio(audio_path, processed_audio_path):
                logger.error("Audio preprocessing failed")
                return None

            # Perform forced alignment with optimized MFA
            textgrid_path = self.mfa.align_audio_text(processed_audio_path, transcript)
            if not textgrid_path:
                logger.error("Forced alignment failed")
                return None

            temp_files.append(textgrid_path)

            # Extract audio features
            audio_features = self.gop_scorer.extract_audio_features(processed_audio_path)

            # Parse alignment results
            phoneme_data = self.gop_scorer.parse_textgrid(textgrid_path)
            if not phoneme_data:
                logger.error("No phoneme data extracted")
                return None

            # Calculate phoneme-level GOP scores
            phoneme_scores = []
            for phoneme_info in phoneme_data:
                gop_score = self.gop_scorer.calculate_phoneme_gop_score(
                    phoneme_info, audio_features
                )

                quality = self.gop_scorer.determine_quality_label(gop_score)

                phoneme_scores.append({
                    'phoneme': phoneme_info['phoneme'],
                    'gop_score': round(gop_score, 2),
                    'quality': quality,
                    'start_time': round(phoneme_info['start_time'], 3),
                    'end_time': round(phoneme_info['end_time'], 3)
                })

            # Calculate overall scores
            overall_score, fluency_score = self.gop_scorer.calculate_overall_scores(phoneme_scores)

            # Calculate statistics
            total_phonemes = len(phoneme_scores)
            total_duration = sum(p['end_time'] - p['start_time'] for p in phoneme_scores)
            average_duration = total_duration / total_phonemes if total_phonemes > 0 else 0

            # Prepare results
            results = {
                'overall_score': round(overall_score, 2),
                'fluency_score': round(fluency_score, 2),
                'phoneme_scores': phoneme_scores,
                'total_phonemes': total_phonemes,
                'average_duration': round(average_duration, 3)
            }

            logger.info(f"Assessment completed successfully: Overall={overall_score:.2f}")
            return results

        except Exception as e:
            logger.error(f"Assessment pipeline failed: {str(e)}")
            return None

        finally:
            # Cleanup
            FileManager.cleanup_files(*temp_files)
            self.mfa.cleanup()
            MemoryManager.force_garbage_collection()

    def __del__(self):
        """Cleanup on deletion."""
        if hasattr(self, 'mfa'):
            self.mfa.cleanup()
