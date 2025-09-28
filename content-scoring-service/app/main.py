"""
Content Scoring Microservice - Main Application
FastAPI-based service for scoring content similarity between questions and answers
"""

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import time
import logging
import json
from prometheus_client import Counter, Histogram, generate_latest
from starlette.responses import Response
import uvicorn

from app.models import ContentScoringRequest, ContentScoringResponse, ErrorResponse
from app.services.scoring_service import ContentScoringService

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Prometheus metrics
REQUEST_COUNT = Counter('content_scoring_requests_total', 'Total content scoring requests')
REQUEST_LATENCY = Histogram('content_scoring_request_duration_seconds', 'Request latency')
ERROR_COUNT = Counter('content_scoring_errors_total', 'Total errors', ['error_type'])

# Initialize FastAPI app
app = FastAPI(
    title="Content Scoring Service",
    description="Microservice for scoring content similarity between questions and answers",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize scoring service
scoring_service = ContentScoringService()

@app.middleware("http")
async def log_requests(request: Request, call_next):
    """Middleware to log requests and measure latency"""
    start_time = time.time()
    
    # Log request
    logger.info(f"Request: {request.method} {request.url}")
    
    response = await call_next(request)
    
    # Calculate and log response time
    process_time = time.time() - start_time
    REQUEST_LATENCY.observe(process_time)
    
    logger.info(f"Response: {response.status_code} - Time: {process_time:.4f}s")
    
    return response

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "content-scoring", "version": "1.0.0"}

@app.get("/metrics")
async def metrics():
    """Prometheus metrics endpoint"""
    return Response(generate_latest(), media_type="text/plain")

@app.post(
    "/api/content-scoring",
    response_model=ContentScoringResponse,
    responses={
        400: {"model": ErrorResponse, "description": "Validation Error"},
        500: {"model": ErrorResponse, "description": "Internal Server Error"}
    }
)
async def score_content(request_data: ContentScoringRequest):
    """
    Score content similarity between question and answer
    
    Args:
        request_data: ContentScoringRequest with question and answer
        
    Returns:
        ContentScoringResponse with score, similarity, and key points
    """
    REQUEST_COUNT.inc()
    
    try:
        logger.info(f"Processing content scoring request")
        
        # Process the scoring request
        result = await scoring_service.score_content(
            question=request_data.question,
            answer=request_data.answer
        )
        
        logger.info(f"Content scoring completed - Score: {result.score}")
        
        return result
        
    except ValueError as e:
        ERROR_COUNT.labels(error_type="validation").inc()
        logger.error(f"Validation error: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
        
    except Exception as e:
        ERROR_COUNT.labels(error_type="internal").inc()
        logger.error(f"Internal server error: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    """Global exception handler"""
    ERROR_COUNT.labels(error_type="unhandled").inc()
    logger.error(f"Unhandled exception: {str(exc)}")
    
    return JSONResponse(
        status_code=500,
        content={"error": "Internal server error"}
    )

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=5001,
        reload=True,
        log_level="info"
    )