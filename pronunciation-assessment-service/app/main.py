"""Main Flask application for pronunciation assessment."""

from flask import Flask, request, jsonify
import os
import logging
from werkzeug.exceptions import RequestEntityTooLarge

from .core import (
    AudioValidator, TextProcessor, FileManager,
    MemoryManager, ResponseFormatter
)
from .services import PronunciationAssessmentPipeline

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize Flask app
app = Flask(__name__)

# Configuration
app.config['MAX_CONTENT_LENGTH'] = 6 * 1024 * 1024  # 6MB max file size
app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', 'pronunciation-assessment-secret-key')

# Initialize assessment pipeline
try:
    pipeline = PronunciationAssessmentPipeline(docker_mode=True)
except RuntimeError as e:
    logger.error("="*80)
    logger.error("CRITICAL ERROR: Failed to initialize Pronunciation Assessment Pipeline")
    logger.error("="*80)
    logger.error(str(e))
    logger.error("="*80)
    logger.error("This service requires Montreal Forced Aligner (MFA) to be installed.")
    logger.error("")
    logger.error("SOLUTION:")
    logger.error("  - Windows: Use Docker (see WINDOWS_SETUP.md)")
    logger.error("  - Linux/Mac: Install via conda (see README.md)")
    logger.error("  - Docker: Run 'docker-compose up' (recommended)")
    logger.error("="*80)
    raise


@app.errorhandler(RequestEntityTooLarge)
def handle_file_too_large(e):
    """Handle file size limit exceeded."""
    return ResponseFormatter.error_response(
        "File size exceeds maximum limit of 6MB", 413
    )


@app.errorhandler(Exception)
def handle_general_error(e):
    """Handle general exceptions."""
    logger.error(f"Unexpected error: {str(e)}")
    return ResponseFormatter.error_response(
        "Internal server error", 500
    )


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint."""
    try:
        memory_usage = MemoryManager.get_memory_usage()
        return jsonify({
            "status": "healthy",
            "memory_usage_mb": round(memory_usage, 2),
            "service": "pronunciation-assessment",
            "version": "1.0.0-optimized"
        })
    except Exception as e:
        logger.error(f"Health check failed: {str(e)}")
        return jsonify({
            "status": "unhealthy",
            "error": str(e)
        }), 500


@app.route('/api/pronunciation-assessment', methods=['POST'])
def assess_pronunciation():
    """
    Main endpoint for pronunciation assessment.

    Expected form data:
    - audio: WAV/MP3/FLAC/M4A file (max 6MB)
    - transcript: Plain text transcript

    Returns:
    JSON response with assessment results
    """
    temp_files = []

    try:
        # Validate request
        if 'audio' not in request.files:
            return ResponseFormatter.error_response(
                "Missing 'audio' file in request", 400
            )

        if 'transcript' not in request.form:
            return ResponseFormatter.error_response(
                "Missing 'transcript' field in request", 400
            )

        audio_file = request.files['audio']
        transcript = request.form['transcript']

        # Validate audio file
        if audio_file.filename == '':
            return ResponseFormatter.error_response(
                "No audio file selected", 400
            )

        if not AudioValidator.allowed_file(audio_file.filename):
            return ResponseFormatter.error_response(
                f"Invalid file type. Allowed: {', '.join(AudioValidator.ALLOWED_EXTENSIONS)}", 400
            )

        # Validate file size
        if not AudioValidator.validate_file_size(audio_file):
            return ResponseFormatter.error_response(
                f"File size exceeds maximum limit of {AudioValidator.MAX_FILE_SIZE // (1024*1024)}MB", 400
            )

        # Validate transcript
        if not TextProcessor.validate_transcript(transcript):
            return ResponseFormatter.error_response(
                "Invalid transcript. Must be non-empty text (max 1000 characters)", 400
            )

        # Check memory before processing
        if not MemoryManager.check_memory_limit(2500):  # Leave 500MB buffer
            MemoryManager.force_garbage_collection()
            if not MemoryManager.check_memory_limit(2500):
                return ResponseFormatter.error_response(
                    "Service temporarily unavailable due to high memory usage", 503
                )

        # Process transcript
        cleaned_transcript = TextProcessor.clean_transcript(transcript)

        # Save uploaded audio file temporarily
        temp_audio_path = FileManager.create_temp_file('.wav')
        temp_files.append(temp_audio_path)

        try:
            audio_file.save(temp_audio_path)
            logger.info(f"Audio file saved: {temp_audio_path}")
        except Exception as e:
            logger.error(f"Failed to save audio file: {str(e)}")
            return ResponseFormatter.error_response(
                "Failed to process audio file", 500
            )

        # Perform pronunciation assessment
        logger.info("Starting pronunciation assessment...")
        assessment_results = pipeline.assess_pronunciation(
            temp_audio_path, cleaned_transcript
        )

        if assessment_results is None:
            logger.error("Assessment pipeline failed")
            error_message = (
                "Pronunciation assessment failed. "
                "This may be due to: "
                "(1) Poor audio quality, "
                "(2) Mismatched transcript, "
                "(3) MFA alignment issues. "
                "Please check logs for details."
            )
            return ResponseFormatter.error_response(error_message, 500)

        # Format response
        response_data = ResponseFormatter.format_assessment_result(
            overall_score=assessment_results['overall_score'],
            fluency_score=assessment_results['fluency_score'],
            phoneme_scores=assessment_results['phoneme_scores'],
            total_phonemes=assessment_results['total_phonemes'],
            average_duration=assessment_results['average_duration']
        )

        logger.info(f"Assessment completed successfully. Overall score: {assessment_results['overall_score']}")

        return jsonify(ResponseFormatter.success_response(
            response_data,
            "Pronunciation assessment completed successfully"
        ))

    except Exception as e:
        logger.error(f"Assessment endpoint error: {str(e)}")
        return ResponseFormatter.error_response(
            "An error occurred during assessment", 500
        )

    finally:
        # Cleanup temporary files
        FileManager.cleanup_files(*temp_files)

        # Force garbage collection to free memory
        MemoryManager.force_garbage_collection()


@app.route('/api/info', methods=['GET'])
def service_info():
    """Get service information."""
    return jsonify({
        "service": "Pronunciation Assessment Microservice",
        "version": "1.0.0-optimized",
        "description": "RESTful API for pronunciation assessment using Montreal Forced Aligner and GOP algorithm",
        "endpoints": {
            "health": "/health",
            "assessment": "/api/pronunciation-assessment",
            "info": "/api/info"
        },
        "supported_audio_formats": list(AudioValidator.ALLOWED_EXTENSIONS),
        "max_file_size_mb": AudioValidator.MAX_FILE_SIZE // (1024 * 1024),
        "features": [
            "Montreal Forced Aligner integration (optimized)",
            "GOP (Goodness of Pronunciation) scoring",
            "Phoneme-level assessment",
            "Memory optimization (≤3GB)",
            "Security validation",
            "Speed optimizations (beam width, single speaker mode)"
        ],
        "optimizations": [
            "Reduced MFA beam width for faster processing",
            "Single speaker mode enabled",
            "60-second timeout (reduced from 300s)",
            "Improved memory management"
        ]
    })


@app.route('/', methods=['GET'])
def root():
    """Root endpoint with basic service information."""
    return jsonify({
        "message": "Pronunciation Assessment Microservice (Optimized)",
        "status": "running",
        "version": "1.0.0-optimized",
        "health_check": "/health",
        "api_info": "/api/info",
        "assessment_endpoint": "/api/pronunciation-assessment"
    })


if __name__ == '__main__':
    logger.info("Starting Pronunciation Assessment Microservice (Optimized)...")
    logger.info(f"Memory limit: 3GB")
    logger.info(f"Max file size: {AudioValidator.MAX_FILE_SIZE // (1024*1024)}MB")
    logger.info(f"Supported formats: {', '.join(AudioValidator.ALLOWED_EXTENSIONS)}")
    logger.info("Optimizations: MFA beam width reduction, single speaker mode, faster timeout")

    # Run the application
    app.run(
        host='0.0.0.0',
        port=5000,
        debug=False,  # Set to False for production
        threaded=True
    )
