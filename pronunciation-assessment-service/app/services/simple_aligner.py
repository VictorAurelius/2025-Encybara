"""Simple speech aligner without external dependencies."""

import os
import logging
import json
from typing import Optional, Dict, List

logger = logging.getLogger(__name__)


class SimpleAligner:
    """Simple mock aligner that doesn't require any heavy dependencies."""

    def __init__(self, device: str = None):
        """Initialize simple aligner."""
        self.device = device or os.environ.get('WHISPERX_DEVICE', 'cpu')
        logger.info(f"Initialized SimpleAligner on {self.device}")

    def align_audio_text(self, audio_path: str, transcript: str) -> Optional[Dict]:
        """
        Simple alignment mock implementation.
        
        Args:
            audio_path: Path to audio file
            transcript: Expected transcript
            
        Returns:
            Mock alignment results
        """
        try:
            logger.info(f"Processing audio with SimpleAligner: {audio_path}")
            
            # Mock word timing based on transcript
            words = transcript.strip().split()
            alignment_data = self._create_mock_alignment(words, transcript)
            
            logger.info(f"SimpleAligner processing successful: {len(alignment_data.get('phonemes', []))} phonemes")
            return alignment_data
            
        except Exception as e:
            logger.error(f"SimpleAligner alignment failed: {str(e)}")
            return None

    def _create_mock_alignment(self, words: List[str], transcript: str) -> Dict:
        """Create mock alignment data."""
        phoneme_data = []
        word_data = []
        current_time = 0.0
        
        for word in words:
            # Mock word timing (0.5 seconds per word)
            word_duration = 0.5
            word_data.append({
                "word": word,
                "start_time": current_time,
                "end_time": current_time + word_duration,
                "confidence": 0.85
            })
            
            # Generate phonemes from words
            phonemes = self._word_to_phonemes(word)
            phoneme_duration = word_duration / max(len(phonemes), 1)
            
            for i, phoneme in enumerate(phonemes):
                start_time = current_time + (i * phoneme_duration)
                end_time = start_time + phoneme_duration
                
                phoneme_data.append({
                    "phoneme": phoneme,
                    "start_time": start_time,
                    "end_time": end_time,
                    "duration": phoneme_duration,
                    "confidence": 0.85
                })
            
            current_time += word_duration
        
        return {
            "phonemes": phoneme_data,
            "words": word_data,
            "recognized_text": " ".join(words),
            "alignment_quality": 0.85
        }

    def _word_to_phonemes(self, word: str) -> List[str]:
        """Simple word to phoneme mapping."""
        phoneme_map = {
            "hello": ["h", "ɛ", "l", "oʊ"],
            "world": ["w", "ɜ", "r", "l", "d"],
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
            "all": ["ɔ", "l"]
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

    def cleanup(self):
        """Clean up resources."""
        logger.debug("SimpleAligner cleanup completed")

    def __del__(self):
        """Cleanup on deletion."""
        self.cleanup()