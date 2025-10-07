"""API response formatting utilities."""


class ResponseFormatter:
    """API response formatting utilities."""

    @staticmethod
    def success_response(data: dict, message: str = "Success") -> dict:
        """Format successful API response."""
        return {
            "success": True,
            "message": message,
            "data": data
        }

    @staticmethod
    def error_response(error: str, code: int = 400) -> tuple:
        """Format error API response."""
        return {
            "success": False,
            "error": error,
            "code": code
        }, code

    @staticmethod
    def format_assessment_result(
        overall_score: float,
        fluency_score: float,
        phoneme_scores: list,
        total_phonemes: int,
        average_duration: float
    ) -> dict:
        """Format pronunciation assessment result."""
        return {
            "overall_score": round(overall_score, 2),
            "fluency_score": round(fluency_score, 2),
            "phoneme_scores": phoneme_scores,
            "total_phonemes": total_phonemes,
            "average_duration": round(average_duration, 3)
        }
