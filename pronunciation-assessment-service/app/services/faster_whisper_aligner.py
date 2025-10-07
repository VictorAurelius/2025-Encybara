"""Faster-Whisper integration as alternative to WhisperX."""

import os
import logging
import numpy as np
from typing import Optional, Dict, List
from faster_whisper import WhisperModel
import librosa

logger = logging.getLogger(__name__)


class FasterWhisperAligner:
    """Faster-Whisper integration as alternative to WhisperX (more stable)."""

    def __init__(self, device: str = None, compute_type: str = None):
        """
        Initialize Faster-Whisper aligner.
        
        Args:
            device: "cpu", "cuda", or "auto" for automatic detection
            compute_type: "float16" for speed, "float32" for accuracy
        """
        self.device = device or os.environ.get('WHISPERX_DEVICE', 'cpu')
        self.compute_type = compute_type or os.environ.get('WHISPERX_COMPUTE_TYPE', 'float32')
        self.model = None
        self._load_models()

    def _load_models(self):
        """Load Faster-Whisper model."""
        try:
            logger.info(f"Loading Faster-Whisper model on {self.device}...")
            
            # Use faster-whisper which is more stable than WhisperX
            self.model = WhisperModel(
                "base", 
                device=self.device,
                compute_type=self.compute_type,
                download_root="/app/models"
            )
            
            logger.info("Faster-Whisper model loaded successfully")
            
        except Exception as e:
            logger.error(f"Failed to load Faster-Whisper model: {str(e)}")
            raise RuntimeError(f"Faster-Whisper model loading failed: {str(e)}")

    def align_audio_text(self, audio_path: str, transcript: str) -> Optional[Dict]:
        """
        Perform transcription and basic alignment using Faster-Whisper.
        
        Args:
            audio_path: Path to audio file
            transcript: Expected transcript
            
        Returns:
            Alignment results with word timestamps or None if failed
        """
        try:
            logger.info(f"Processing audio with Faster-Whisper: {audio_path}")
            
            # Transcribe with Faster-Whisper
            segments, info = self.model.transcribe(
                audio_path,
                beam_size=5,
                word_timestamps=True,
                language="en"
            )
            
            # Convert segments to list
            segments_list = list(segments)
            
            # Convert to our format
            alignment_data = self._convert_faster_whisper_format(segments_list, transcript)
            
            logger.info(f"Faster-Whisper transcription successful: {len(alignment_data.get('phonemes', []))} phonemes")
            return alignment_data
            
        except Exception as e:
            logger.error(f"Faster-Whisper alignment failed: {str(e)}")
            return None

    def _convert_faster_whisper_format(self, segments: List, expected_transcript: str) -> Dict:
        """
        Convert Faster-Whisper results to our internal format.
        
        Args:
            segments: Faster-Whisper segments
            expected_transcript: Original transcript for validation
            
        Returns:
            Converted alignment data
        """
        phoneme_data = []
        word_data = []
        recognized_words = []
        
        try:
            for segment in segments:
                segment_text = segment.text.strip()
                recognized_words.append(segment_text)
                
                # Process words within segment
                for word in segment.words:
                    if hasattr(word, 'start') and hasattr(word, 'end'):
                        word_data.append({
                            "word": word.word.strip(),
                            "start_time": float(word.start),
                            "end_time": float(word.end),
                            "confidence": getattr(word, 'probability', 1.0)
                        })
                        
                        # Generate phoneme approximations from words
                        phonemes = self._word_to_phonemes(word.word.strip())
                        word_duration = float(word.end) - float(word.start)
                        phoneme_duration = word_duration / max(len(phonemes), 1)
                        
                        for i, phoneme in enumerate(phonemes):
                            start_time = float(word.start) + (i * phoneme_duration)
                            end_time = start_time + phoneme_duration
                            
                            phoneme_data.append({
                                "phoneme": phoneme,
                                "start_time": start_time,
                                "end_time": end_time,
                                "duration": phoneme_duration,
                                "confidence": getattr(word, 'probability', 1.0)
                            })
            
            recognized_text = " ".join(recognized_words)
            alignment_quality = self._calculate_alignment_quality(word_data, expected_transcript)
            
            return {
                "phonemes": phoneme_data,
                "words": word_data,
                "recognized_text": recognized_text,
                "alignment_quality": alignment_quality
            }
            
        except Exception as e:
            logger.error(f"Faster-Whisper conversion failed: {str(e)}")
            return {"phonemes": [], "words": [], "recognized_text": "", "alignment_quality": 0.0}

    def _word_to_phonemes(self, word: str) -> List[str]:
        """Simple word to phoneme mapping (same as WhisperX version)."""
        # Reuse the phoneme mapping from WhisperX aligner
        phoneme_map = {
            "the": ["ð", "ə"], "and": ["æ", "n", "d"], "you": ["y", "u"],
            "that": ["ð", "æ", "t"], "it": ["ɪ", "t"], "to": ["t", "u"],
            "of": ["ʌ", "v"], "in": ["ɪ", "n"], "for": ["f", "ɔ", "r"],
            "is": ["ɪ", "z"], "on": ["ɑ", "n"], "as": ["æ", "z"],
            "be": ["b", "i"], "or": ["ɔ", "r"], "an": ["æ", "n"],
            "are": ["ɑ", "r"], "by": ["b", "aɪ"], "this": ["ð", "ɪ", "s"],
            "have": ["h", "æ", "v"], "from": ["f", "r", "ʌ", "m"],
            "they": ["ð", "eɪ"], "we": ["w", "i"], "say": ["s", "eɪ"],
            "her": ["h", "ɜ", "r"], "she": ["ʃ", "i"], "will": ["w", "ɪ", "l"],
            "my": ["m", "aɪ"], "one": ["w", "ʌ", "n"], "all": ["ɔ", "l"],
            "hello": ["h", "ɛ", "l", "oʊ"], "world": ["w", "ɜ", "r", "l", "d"]
        }
        
        word_lower = word.lower().strip()
        if word_lower in phoneme_map:
            return phoneme_map[word_lower]
        
        # Fallback: simple letter mapping
        letter_phoneme_map = {
            'a': 'æ', 'b': 'b', 'c': 'k', 'd': 'd', 'e': 'ɛ',
            'f': 'f', 'g': 'g', 'h': 'h', 'i': 'ɪ', 'j': 'dʒ',
            'k': 'k', 'l': 'l', 'm': 'm', 'n': 'n', 'o': 'ɑ',
            'p': 'p', 'q': 'kw', 'r': 'r', 's': 's', 't': 't',
            'u': 'ʌ', 'v': 'v', 'w': 'w', 'x': 'ks', 'y': 'j', 'z': 'z'
        }
        
        phonemes = []
        for char in word_lower:
            if char.isalpha():
                phonemes.append(letter_phoneme_map.get(char, char))
        
        return phonemes if phonemes else [word_lower]

    def _calculate_alignment_quality(self, word_data: List[Dict], expected_transcript: str) -> float:
        """Calculate alignment quality score."""
        try:
            recognized_words = [w["word"].lower().strip() for w in word_data]
            expected_words = expected_transcript.lower().split()
            
            if not expected_words:
                return 0.0
            
            matches = 0
            for expected_word in expected_words:
                if expected_word in recognized_words:
                    matches += 1
            
            quality = matches / len(expected_words)
            return min(1.0, max(0.0, quality))
            
        except Exception:
            return 0.5

    def cleanup(self):
        """Clean up resources."""
        try:
            logger.debug("Faster-Whisper aligner cleanup completed")
        except Exception as e:
            logger.warning(f"Cleanup warning: {str(e)}")

    def __del__(self):
        """Cleanup on deletion."""
        self.cleanup()