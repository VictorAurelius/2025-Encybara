"""OpenAI Whisper integration - no ctranslate2 dependencies."""

import os
import logging
import whisper
from typing import Optional, Dict, List

logger = logging.getLogger(__name__)


class OpenAIWhisperAligner:
    """OpenAI Whisper integration without ctranslate2 dependencies."""

    def __init__(self, device: str = None):
        """Initialize OpenAI Whisper model."""
        self.device = device or os.environ.get('WHISPERX_DEVICE', 'cpu')
        self.model = None
        self._load_models()

    def _load_models(self):
        """Load OpenAI Whisper model."""
        try:
            logger.info(f"Loading OpenAI Whisper model on {self.device}...")
            
            # Load OpenAI Whisper model (base for balance of speed/accuracy)
            self.model = whisper.load_model("base", device=self.device)
            
            logger.info("OpenAI Whisper model loaded successfully")
            
        except Exception as e:
            logger.error(f"Failed to load OpenAI Whisper model: {str(e)}")
            raise RuntimeError(f"OpenAI Whisper model loading failed: {str(e)}")

    def align_audio_text(self, audio_path: str, transcript: str) -> Optional[Dict]:
        """
        Perform transcription using OpenAI Whisper.
        
        Args:
            audio_path: Path to audio file
            transcript: Expected transcript
            
        Returns:
            Alignment results with word timestamps or None if failed
        """
        try:
            logger.info(f"Processing audio with OpenAI Whisper: {audio_path}")
            
            # Transcribe with OpenAI Whisper
            result = self.model.transcribe(
                audio_path,
                language="en",
                word_timestamps=True
            )
            
            # Convert to our format
            alignment_data = self._convert_whisper_format(result, transcript)
            
            logger.info(f"OpenAI Whisper transcription successful: {len(alignment_data.get('phonemes', []))} phonemes")
            return alignment_data
            
        except Exception as e:
            logger.error(f"OpenAI Whisper alignment failed: {str(e)}")
            return None

    def _convert_whisper_format(self, whisper_result: Dict, expected_transcript: str) -> Dict:
        """Convert OpenAI Whisper results to our internal format."""
        phoneme_data = []
        word_data = []
        
        try:
            segments = whisper_result.get("segments", [])
            
            for segment in segments:
                words = segment.get("words", [])
                
                for word_info in words:
                    if "start" in word_info and "end" in word_info:
                        word_data.append({
                            "word": word_info["word"].strip(),
                            "start_time": float(word_info["start"]),
                            "end_time": float(word_info["end"]),
                            "confidence": word_info.get("probability", 1.0)
                        })
                        
                        # Generate phonemes from words
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
                                "confidence": word_info.get("probability", 1.0)
                            })
            
            recognized_text = " ".join([w["word"] for w in word_data])
            alignment_quality = self._calculate_alignment_quality(word_data, expected_transcript)
            
            return {
                "phonemes": phoneme_data,
                "words": word_data,
                "recognized_text": recognized_text,
                "alignment_quality": alignment_quality
            }
            
        except Exception as e:
            logger.error(f"Whisper conversion failed: {str(e)}")
            return {"phonemes": [], "words": [], "recognized_text": "", "alignment_quality": 0.0}

    def _word_to_phonemes(self, word: str) -> List[str]:
        """Simple word to phoneme mapping."""
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
            logger.debug("OpenAI Whisper aligner cleanup completed")
        except Exception as e:
            logger.warning(f"Cleanup warning: {str(e)}")

    def __del__(self):
        """Cleanup on deletion."""
        self.cleanup()