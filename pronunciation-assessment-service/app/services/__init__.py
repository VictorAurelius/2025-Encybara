"""Pronunciation assessment services."""

from .gop_scorer import GOPScorer
from .assessment_pipeline import PronunciationAssessmentPipeline

# Optional imports
try:
    from .whisperx_aligner import WhisperXAligner
    __all__ = ['WhisperXAligner', 'GOPScorer', 'PronunciationAssessmentPipeline']
except ImportError:
    __all__ = ['GOPScorer', 'PronunciationAssessmentPipeline']

try:
    from .faster_whisper_aligner import FasterWhisperAligner
    if 'FasterWhisperAligner' not in __all__:
        __all__.append('FasterWhisperAligner')
except ImportError:
    pass
