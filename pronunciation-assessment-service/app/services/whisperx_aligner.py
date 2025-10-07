"""WhisperX integration for fast forced alignment."""

import os
import logging
import tempfile
import numpy as np
from typing import Optional, Dict, List, Tuple
import whisperx
import torch

logger = logging.getLogger(__name__)


class WhisperXAligner:
    """WhisperX integration for fast forced alignment replacing MFA."""

    def __init__(self, device: str = None, compute_type: str = None):
        """
        Initialize WhisperX aligner.
        
        Args:
            device: "cpu", "cuda", or "auto" for automatic detection
            compute_type: "float16" for speed, "float32" for accuracy
        """
        # Use environment variables if not specified
        self.device = self._detect_device(device or os.environ.get('WHISPERX_DEVICE', 'auto'))
        self.compute_type = compute_type or os.environ.get('WHISPERX_COMPUTE_TYPE', 'float16')
        self.model = None
        self.align_model = None
        self.metadata = None
        self._load_models()

    def _detect_device(self, device: str) -> str:
        """Detect optimal device for processing."""
        if device == "auto":
            if torch.cuda.is_available():
                return "cuda"
            else:
                return "cpu"
        return device

    def _load_models(self):
        """Load WhisperX models with optimizations."""
        try:
            # Load transcription model (using base for balance of speed/accuracy)
            logger.info(f"Loading WhisperX model on {self.device}...")
            self.model = whisperx.load_model(
                "base", 
                device=self.device,
                compute_type=self.compute_type
            )
            
            # Load alignment model for English
            logger.info("Loading alignment model...")
            self.align_model, self.metadata = whisperx.load_align_model(
                language_code="en", 
                device=self.device
            )
            
            logger.info("WhisperX models loaded successfully")
            
        except Exception as e:
            logger.error(f"Failed to load WhisperX models: {str(e)}")
            raise RuntimeError(f"WhisperX model loading failed: {str(e)}")

    def align_audio_text(self, audio_path: str, transcript: str) -> Optional[Dict]:
        """
        Perform forced alignment using WhisperX.
        
        Args:
            audio_path: Path to audio file
            transcript: Expected transcript
            
        Returns:
            Alignment results with word and phoneme timestamps or None if failed
        """
        try:
            # Load audio
            logger.info(f"Processing audio: {audio_path}")
            audio = whisperx.load_audio(audio_path)
            
            # Transcribe with WhisperX
            logger.info("Transcribing with WhisperX...")
            result = self.model.transcribe(audio, batch_size=16)
            
            # Align with provided transcript
            logger.info("Performing forced alignment...")
            aligned_result = whisperx.align(
                result["segments"], 
                self.align_model, 
                self.metadata, 
                audio, 
                self.device, 
                return_char_alignments=False
            )
            
            # Convert to our format
            alignment_data = self._convert_alignment_format(aligned_result, transcript)
            
            logger.info(f"Alignment successful: {len(alignment_data.get('phonemes', []))} phonemes")
            return alignment_data
            
        except Exception as e:
            logger.error(f"WhisperX alignment failed: {str(e)}")
            return None

    def _convert_alignment_format(self, whisperx_result: Dict, expected_transcript: str) -> Dict:
        """
        Convert WhisperX alignment results to our internal format.
        
        Args:
            whisperx_result: WhisperX alignment results
            expected_transcript: Original transcript for validation
            
        Returns:
            Converted alignment data
        """
        phoneme_data = []
        word_data = []
        
        try:
            segments = whisperx_result.get("segments", [])
            
            for segment in segments:
                words = segment.get("words", [])
                
                for word_info in words:
                    if "start" in word_info and "end" in word_info:
                        word_data.append({
                            "word": word_info["word"].strip(),
                            "start_time": float(word_info["start"]),
                            "end_time": float(word_info["end"]),
                            "confidence": word_info.get("score", 1.0)
                        })
                        
                        # Generate phoneme approximations from words
                        # This is a simplified approach - for more accuracy, 
                        # you could integrate a phoneme dictionary
                        phonemes = self._word_to_phonemes(word_info["word"].strip())
                        word_duration = float(word_info["end"]) - float(word_info["start"])
                        phoneme_duration = word_duration / max(len(phonemes), 1)
                        
                        for i, phoneme in enumerate(phonemes):
                            start_time = float(word_info["start"]) + (i * phoneme_duration)
                            end_time = start_time + phoneme_duration
                            
                            phoneme_data.append({
                                "phoneme": phoneme,
                                "start_time": start_time,
                                "end_time": end_time,
                                "duration": phoneme_duration,
                                "confidence": word_info.get("score", 1.0)
                            })
            
            return {
                "phonemes": phoneme_data,
                "words": word_data,
                "recognized_text": " ".join([w["word"] for w in word_data]),
                "alignment_quality": self._calculate_alignment_quality(word_data, expected_transcript)
            }
            
        except Exception as e:
            logger.error(f"Alignment conversion failed: {str(e)}")
            return {"phonemes": [], "words": [], "recognized_text": "", "alignment_quality": 0.0}

    def _word_to_phonemes(self, word: str) -> List[str]:
        """
        Convert word to phonemes using simple mapping.
        
        For production use, consider integrating:
        - CMU Pronouncing Dictionary
        - phonemizer library
        - g2p (grapheme-to-phoneme) models
        
        Args:
            word: Input word
            
        Returns:
            List of phonemes
        """
        # Simple phoneme mapping for common words
        # This is a basic implementation - expand as needed
        phoneme_map = {
            "the": ["ð", "ə"],
            "and": ["æ", "n", "d"],
            "you": ["y", "u"],
            "that": ["ð", "æ", "t"],
            "it": ["ɪ", "t"],
            "to": ["t", "u"],
            "of": ["ʌ", "v"],
            "in": ["ɪ", "n"],
            "for": ["f", "ɔ", "r"],
            "is": ["ɪ", "z"],
            "on": ["ɑ", "n"],
            "as": ["æ", "z"],
            "be": ["b", "i"],
            "or": ["ɔ", "r"],
            "an": ["æ", "n"],
            "are": ["ɑ", "r"],
            "by": ["b", "aɪ"],
            "this": ["ð", "ɪ", "s"],
            "have": ["h", "æ", "v"],
            "from": ["f", "r", "ʌ", "m"],
            "they": ["ð", "eɪ"],
            "we": ["w", "i"],
            "say": ["s", "eɪ"],
            "her": ["h", "ɜ", "r"],
            "she": ["ʃ", "i"],
            "will": ["w", "ɪ", "l"],
            "my": ["m", "aɪ"],
            "one": ["w", "ʌ", "n"],
            "all": ["ɔ", "l"],
            "would": ["w", "ʊ", "d"],
            "there": ["ð", "ɛ", "r"]
        }
        
        word_lower = word.lower().strip()
        if word_lower in phoneme_map:
            return phoneme_map[word_lower]
        
        # Fallback: simple letter-to-phoneme mapping
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
        """
        Calculate alignment quality by comparing recognized text with expected.
        
        Args:
            word_data: List of word alignment data
            expected_transcript: Expected transcript
            
        Returns:
            Quality score (0.0 to 1.0)
        """
        try:
            recognized_words = [w["word"].lower().strip() for w in word_data]
            expected_words = expected_transcript.lower().split()
            
            if not expected_words:
                return 0.0
            
            # Simple word matching
            matches = 0
            for expected_word in expected_words:
                if expected_word in recognized_words:
                    matches += 1
            
            quality = matches / len(expected_words)
            return min(1.0, max(0.0, quality))
            
        except Exception:
            return 0.5  # Default quality score

    def cleanup(self):
        """Clean up resources."""
        try:
            # Clear GPU memory if using CUDA
            if self.device == "cuda":
                torch.cuda.empty_cache()
            
            logger.debug("WhisperX aligner cleanup completed")
        except Exception as e:
            logger.warning(f"Cleanup warning: {str(e)}")

    def __del__(self):
        """Cleanup on deletion."""
        self.cleanup()