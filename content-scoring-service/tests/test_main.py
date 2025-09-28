"""
Test cases for main FastAPI application
"""

import pytest
from fastapi.testclient import TestClient
import json
from unittest.mock import patch, MagicMock

from app.main import app
from app.models import ContentScoringResponse, KeyPoint

client = TestClient(app)

class TestHealthEndpoint:
    """Test cases for health check endpoint"""
    
    def test_health_check(self):
        """Test health check endpoint returns correct response"""
        response = client.get("/health")
        
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "healthy"
        assert data["service"] == "content-scoring"
        assert data["version"] == "1.0.0"

class TestMetricsEndpoint:
    """Test cases for metrics endpoint"""
    
    def test_metrics_endpoint(self):
        """Test metrics endpoint returns Prometheus format"""
        response = client.get("/metrics")
        
        assert response.status_code == 200
        assert "text/plain" in response.headers["content-type"]

class TestContentScoringEndpoint:
    """Test cases for content scoring endpoint"""
    
    @patch('app.main.scoring_service')
    def test_valid_content_scoring_request(self, mock_service):
        """Test valid content scoring request"""
        # Mock the scoring service response
        mock_response = ContentScoringResponse(
            success=True,
            score=85.5,
            similarity=0.855,
            key_points=[
                KeyPoint(point="machine learning", present=True),
                KeyPoint(point="artificial intelligence", present=False)
            ]
        )
        mock_service.score_content.return_value = mock_response
        
        # Test request
        test_data = {
            "question": "What is machine learning?",
            "answer": "Machine learning is a subset of artificial intelligence that uses algorithms to learn patterns from data."
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["score"] == 85.5
        assert data["similarity"] == 0.855
        assert len(data["key_points"]) == 2
        assert data["key_points"][0]["point"] == "machine learning"
        assert data["key_points"][0]["present"] is True
    
    def test_empty_question_validation(self):
        """Test validation for empty question"""
        test_data = {
            "question": "",
            "answer": "This is a valid answer."
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 422  # Validation error
    
    def test_empty_answer_validation(self):
        """Test validation for empty answer"""
        test_data = {
            "question": "What is artificial intelligence?",
            "answer": ""
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 422  # Validation error
    
    def test_question_too_long(self):
        """Test validation for question exceeding max length"""
        long_question = "a" * 513  # Exceeds 512 character limit
        test_data = {
            "question": long_question,
            "answer": "Valid answer"
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 422  # Validation error
    
    def test_answer_too_long(self):
        """Test validation for answer exceeding max length"""
        long_answer = "a" * 1025  # Exceeds 1024 character limit
        test_data = {
            "question": "Valid question?",
            "answer": long_answer
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 422  # Validation error
    
    def test_missing_question_field(self):
        """Test validation for missing question field"""
        test_data = {
            "answer": "Valid answer"
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 422  # Validation error
    
    def test_missing_answer_field(self):
        """Test validation for missing answer field"""
        test_data = {
            "question": "Valid question?"
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 422  # Validation error
    
    @patch('app.main.scoring_service')
    def test_scoring_service_error(self, mock_service):
        """Test handling of scoring service errors"""
        # Mock the scoring service to raise an exception
        mock_service.score_content.side_effect = ValueError("Scoring failed")
        
        test_data = {
            "question": "What is machine learning?",
            "answer": "Machine learning is about data."
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 400
        data = response.json()
        assert "error" in data
    
    @patch('app.main.scoring_service')
    def test_internal_server_error(self, mock_service):
        """Test handling of internal server errors"""
        # Mock the scoring service to raise a general exception
        mock_service.score_content.side_effect = Exception("Internal error")
        
        test_data = {
            "question": "What is machine learning?",
            "answer": "Machine learning is about data."
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 500
        data = response.json()
        assert "error" in data
    
    def test_whitespace_only_question(self):
        """Test validation for question with only whitespace"""
        test_data = {
            "question": "   \n\t   ",
            "answer": "Valid answer"
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 422  # Should be caught by Pydantic validation
    
    def test_whitespace_only_answer(self):
        """Test validation for answer with only whitespace"""
        test_data = {
            "question": "Valid question?",
            "answer": "   \n\t   "
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 422  # Should be caught by Pydantic validation
    
    @patch('app.main.scoring_service')
    def test_special_characters_handling(self, mock_service):
        """Test handling of special characters in input"""
        mock_response = ContentScoringResponse(
            success=True,
            score=75.0,
            similarity=0.75,
            key_points=[KeyPoint(point="test", present=True)]
        )
        mock_service.score_content.return_value = mock_response
        
        test_data = {
            "question": "What is AI? 🤖 #artificial-intelligence @mention",
            "answer": "AI is... (artificial intelligence) & machine learning! 50% automation."
        }
        
        response = client.post("/api/content-scoring", json=test_data)
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True

class TestMiddleware:
    """Test cases for middleware functionality"""
    
    def test_request_logging_middleware(self):
        """Test that requests are logged properly"""
        # This is more of an integration test
        response = client.get("/health")
        assert response.status_code == 200

class TestDocumentation:
    """Test cases for API documentation"""
    
    def test_openapi_docs_available(self):
        """Test that OpenAPI documentation is available"""
        response = client.get("/docs")
        assert response.status_code == 200
    
    def test_redoc_docs_available(self):
        """Test that ReDoc documentation is available"""
        response = client.get("/redoc")
        assert response.status_code == 200
    
    def test_openapi_json_available(self):
        """Test that OpenAPI JSON schema is available"""
        response = client.get("/openapi.json")
        assert response.status_code == 200
        data = response.json()
        assert "openapi" in data
        assert data["info"]["title"] == "Content Scoring Service"