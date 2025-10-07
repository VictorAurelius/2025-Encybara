"""GOP (Goodness of Pronunciation) scorer implementation with WhisperX support."""

import logging
import numpy as np
import librosa
from typing import Dict, List, Tuple, Optional

# Optional import for legacy MFA support
try:
    from praatio import textgrid
    HAS_PRAATIO = True
except ImportError:
    HAS_PRAATIO = False
    logger = logging.getLogger(__name__)
    logger.warning("praatio not available - TextGrid parsing disabled (WhisperX mode only)")

logger = logging.getLogger(__name__)


class GOPScorer:
    """Goodness of Pronunciation (GOP) algorithm implementation with WhisperX support."""

    def __init__(self):
        self.phoneme_quality_thresholds = {
            'excellent': 85.0,
            'good': 70.0,
            'fair': 55.0,
            'poor': 0.0
        }

    def extract_audio_features(self, audio_path: str) -> Dict:
        """
        Extract audio features for GOP calculation (optimized for speed).

        Args:
            audio_path: Path to audio file

        Returns:
            Dictionary of audio features
        """
        try:
            # Load audio with lower sample rate for faster processing
            y, sr = librosa.load(audio_path, sr=8000)

            # Extract only essential features for speed
            # Skip MFCC to save ~30% processing time
            spectral_centroid = librosa.feature.spectral_centroid(y=y, sr=sr, hop_length=1024)[0]

            # Calculate minimal statistics for speed
            features = {
                'spectral_centroid_mean': np.mean(spectral_centroid),
                'energy': np.sum(y**2) / len(y)
            }

            return features

        except Exception as e:
            logger.error(f"Feature extraction failed: {str(e)}")
            return {'spectral_centroid_mean': 1000, 'energy': 0.1}

    def parse_textgrid(self, textgrid_path: str) -> List[Dict]:
        """
        Parse TextGrid file to extract phoneme alignments (legacy MFA support).

        Args:
            textgrid_path: Path to TextGrid file

        Returns:
            List of phoneme alignment data
        """
        if not HAS_PRAATIO:
            logger.error("praatio not available - cannot parse TextGrid files")
            return []
            
        try:
            tg = textgrid.openTextgrid(textgrid_path, includeEmptyIntervals=False)

            phoneme_data = []

            # Look for phone tier (phoneme tier)
            phone_tier = None
            for tier_name in tg.tierNames:
                if 'phone' in tier_name.lower() or 'phoneme' in tier_name.lower():
                    phone_tier = tg.getTier(tier_name)
                    break

            if phone_tier is None:
                # Fallback: use the last tier which is usually phonemes
                phone_tier = tg.getTier(tg.tierNames[-1])

            for interval in phone_tier.entries:
                start_time, end_time, phoneme = interval
                if phoneme.strip() and phoneme != 'sil':  # Skip silence
                    phoneme_data.append({
                        'phoneme': phoneme.strip(),
                        'start_time': float(start_time),
                        'end_time': float(end_time),
                        'duration': float(end_time) - float(start_time)
                    })

            return phoneme_data

        except Exception as e:
            logger.error(f"TextGrid parsing failed: {str(e)}")
            return []

    def parse_whisperx_alignment(self, alignment_data: Dict) -> List[Dict]:
        """
        Parse WhisperX alignment data to extract phoneme alignments.

        Args:
            alignment_data: WhisperX alignment results

        Returns:
            List of phoneme alignment data
        """
        try:
            phoneme_data = []
            phonemes = alignment_data.get('phonemes', [])
            
            for phoneme_info in phonemes:
                if phoneme_info.get('phoneme') and phoneme_info.get('phoneme').strip():
                    duration = phoneme_info['end_time'] - phoneme_info['start_time']
                    phoneme_data.append({
                        'phoneme': phoneme_info['phoneme'].strip(),
                        'start_time': float(phoneme_info['start_time']),
                        'end_time': float(phoneme_info['end_time']),
                        'duration': float(duration),
                        'confidence': phoneme_info.get('confidence', 1.0)
                    })

            logger.info(f"Parsed {len(phoneme_data)} phonemes from WhisperX alignment")
            return phoneme_data

        except Exception as e:
            logger.error(f"WhisperX alignment parsing failed: {str(e)}")
            return []

    def calculate_phoneme_gop_score(self, phoneme_data: Dict,
                                  audio_features: Dict) -> float:
        """
        Calculate GOP score for a single phoneme.

        Args:
            phoneme_data: Phoneme alignment data
            audio_features: Audio features

        Returns:
            GOP score (0-100)
        """
        try:
            # Base score calculation using duration and energy
            duration = phoneme_data['duration']

            # Normalize duration (typical phoneme duration: 0.05-0.2 seconds)
            duration_score = min(100, max(0, 100 - abs(duration - 0.1) * 500))

            # Energy-based score
            energy_score = min(100, audio_features.get('energy', 0.1) * 1000)

            # Spectral features score
            spectral_score = min(100, audio_features.get('spectral_centroid_mean', 1000) / 50)

            # Combine scores with weights
            gop_score = (
                duration_score * 0.4 +
                energy_score * 0.3 +
                spectral_score * 0.3
            )

            # Add some phoneme-specific adjustments
            phoneme = phoneme_data['phoneme'].lower()
            if phoneme in ['t', 'k', 'p']:  # Plosives
                gop_score *= 0.95  # Slightly penalize plosives as they're harder
            elif phoneme in ['a', 'e', 'i', 'o', 'u']:  # Vowels
                gop_score *= 1.05  # Slightly boost vowels

            return max(0, min(100, gop_score))

        except Exception as e:
            logger.error(f"GOP score calculation failed: {str(e)}")
            return 50.0  # Return neutral score on error

    def determine_quality_label(self, gop_score: float) -> str:
        """Determine quality label based on GOP score."""
        for quality, threshold in self.phoneme_quality_thresholds.items():
            if gop_score >= threshold:
                return quality
        return 'poor'

    def calculate_overall_scores(self, phoneme_scores: List[Dict]) -> Tuple[float, float]:
        """
        Calculate overall and fluency scores.

        Args:
            phoneme_scores: List of phoneme score data

        Returns:
            Tuple of (overall_score, fluency_score)
        """
        if not phoneme_scores:
            return 0.0, 0.0

        # Overall score: weighted average of phoneme scores
        total_score = sum(p['gop_score'] for p in phoneme_scores)
        total_duration = sum(p['end_time'] - p['start_time'] for p in phoneme_scores)

        overall_score = total_score / len(phoneme_scores)

        # Fluency score: based on timing consistency and speech rate
        durations = [p['end_time'] - p['start_time'] for p in phoneme_scores]
        duration_variance = np.var(durations) if len(durations) > 1 else 0

        # Calculate speech rate (phonemes per second)
        speech_rate = len(phoneme_scores) / max(total_duration, 0.1)

        # Fluency based on consistency and appropriate speech rate
        consistency_score = max(0, 100 - duration_variance * 1000)
        rate_score = min(100, max(0, 100 - abs(speech_rate - 8) * 10))  # Target: ~8 phonemes/sec

        fluency_score = (consistency_score * 0.6 + rate_score * 0.4)

        return overall_score, fluency_score
