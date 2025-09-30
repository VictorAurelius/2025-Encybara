import os
import tempfile
import subprocess
import logging
import numpy as np
import librosa
from typing import Dict, List, Tuple, Optional
from praatio import textgrid
from utils import FileManager, MemoryManager, AudioValidator
import json

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class MontrealForcedAligner:
    """Montreal Forced Aligner integration for forced alignment."""
    
    def __init__(self, acoustic_model: str = "english_us_arpa", 
                 dictionary: str = "english_us_arpa"):
        self.acoustic_model = acoustic_model
        self.dictionary = dictionary
        self.temp_dir = tempfile.mkdtemp()
    
    def align_audio_text(self, audio_path: str, transcript: str) -> Optional[str]:
        """
        Perform forced alignment using MFA.
        
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
            
            # Run MFA alignment
            mfa_command = [
                'mfa', 'align',
                corpus_dir,
                self.dictionary,
                self.acoustic_model,
                output_dir,
                '--clean'
            ]
            
            logger.info(f"Running MFA command: {' '.join(mfa_command)}")
            result = subprocess.run(
                mfa_command,
                capture_output=True,
                text=True,
                timeout=300  # 5 minute timeout
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
            logger.error("MFA alignment timed out")
            return None
        except Exception as e:
            logger.error(f"MFA alignment error: {str(e)}")
            return None
    
    def cleanup(self):
        """Clean up temporary files."""
        import shutil
        try:
            shutil.rmtree(self.temp_dir)
            logger.debug(f"Cleaned up MFA temp directory: {self.temp_dir}")
        except Exception as e:
            logger.warning(f"Failed to cleanup MFA temp directory: {str(e)}")

class GOPScorer:
    """Goodness of Pronunciation (GOP) algorithm implementation."""
    
    def __init__(self):
        self.phoneme_quality_thresholds = {
            'excellent': 85.0,
            'good': 70.0,
            'fair': 55.0,
            'poor': 0.0
        }
    
    def extract_audio_features(self, audio_path: str) -> Dict:
        """
        Extract audio features for GOP calculation.
        
        Args:
            audio_path: Path to audio file
            
        Returns:
            Dictionary of audio features
        """
        try:
            # Load audio
            y, sr = librosa.load(audio_path, sr=16000)
            
            # Extract features
            mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=13)
            spectral_centroid = librosa.feature.spectral_centroid(y=y, sr=sr)[0]
            spectral_rolloff = librosa.feature.spectral_rolloff(y=y, sr=sr)[0]
            zero_crossing_rate = librosa.feature.zero_crossing_rate(y)[0]
            
            # Calculate statistics
            features = {
                'mfcc_mean': np.mean(mfcc, axis=1),
                'mfcc_std': np.std(mfcc, axis=1),
                'spectral_centroid_mean': np.mean(spectral_centroid),
                'spectral_rolloff_mean': np.mean(spectral_rolloff),
                'zcr_mean': np.mean(zero_crossing_rate),
                'energy': np.sum(y**2) / len(y)
            }
            
            return features
            
        except Exception as e:
            logger.error(f"Feature extraction failed: {str(e)}")
            return {}
    
    def parse_textgrid(self, textgrid_path: str) -> List[Dict]:
        """
        Parse TextGrid file to extract phoneme alignments.
        
        Args:
            textgrid_path: Path to TextGrid file
            
        Returns:
            List of phoneme alignment data
        """
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

class PronunciationAssessmentPipeline:
    """Complete pronunciation assessment pipeline."""
    
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
            
            # Perform forced alignment
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