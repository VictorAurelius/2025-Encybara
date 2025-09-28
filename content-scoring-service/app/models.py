"""
Pydantic models for Content Scoring Service
Defines request and response schemas with validation
"""

from pydantic import BaseModel, Field, validator
from typing import List
import re

class KeyPoint(BaseModel):
    """Model for key point analysis"""
    point: str = Field(..., description="Keyword or key phrase")
    present: bool = Field(..., description="Whether the key point is present in the answer")

class ContentScoringRequest(BaseModel):
    """Request model for content scoring endpoint"""
    question: str = Field(
        ..., 
        min_length=1,
        max_length=512,
        description="The question text (max 512 characters)"
    )
    answer: str = Field(
        ..., 
        min_length=1,
        max_length=1024,
        description="The answer text (max 1024 characters)"
    )
    
    @validator('question')
    def validate_question(cls, v):
        """Validate question field"""
        if not v or not v.strip():
            raise ValueError("Question cannot be empty or contain only whitespace")
        
        # Remove extra whitespace
        v = re.sub(r'\s+', ' ', v.strip())
        
        if len(v) > 512:
            raise ValueError("Question cannot exceed 512 characters")
        
        return v
    
    @validator('answer')
    def validate_answer(cls, v):
        """Validate answer field"""
        if not v or not v.strip():
            raise ValueError("Answer cannot be empty or contain only whitespace")
        
        # Remove extra whitespace
        v = re.sub(r'\s+', ' ', v.strip())
        
        if len(v) > 1024:
            raise ValueError("Answer cannot exceed 1024 characters")
        
        return v

class ContentScoringResponse(BaseModel):
    """Response model for content scoring endpoint"""
    success: bool = Field(True, description="Whether the request was successful")
    score: float = Field(
        ..., 
        ge=0.0, 
        le=100.0,
        description="Content similarity score (0-100)"
    )
    similarity: float = Field(
        ..., 
        ge=0.0, 
        le=1.0,
        description="Cosine similarity value (0-1)"
    )
    key_points: List[KeyPoint] = Field(
        ..., 
        description="List of key points and their presence in the answer"
    )
    
    class Config:
        """Pydantic configuration"""
        schema_extra = {
            "example": {
                "success": True,
                "score": 85.5,
                "similarity": 0.855,
                "key_points": [
                    {"point": "machine learning", "present": True},
                    {"point": "artificial intelligence", "present": False}
                ]
            }
        }

class ErrorResponse(BaseModel):
    """Error response model"""
    error: str = Field(..., description="Error message")
    
    class Config:
        """Pydantic configuration"""
        schema_extra = {
            "example": {
                "error": "Question cannot exceed 512 characters"
            }
        }

class HealthResponse(BaseModel):
    """Health check response model"""
    status: str = Field(..., description="Service status")
    service: str = Field(..., description="Service name")
    version: str = Field(..., description="Service version")
    
    class Config:
        """Pydantic configuration"""
        schema_extra = {
            "example": {
                "status": "healthy",
                "service": "content-scoring",
                "version": "1.0.0"
            }
        }