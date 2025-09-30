"""
Test cases for Pydantic models
"""

import pytest
from pydantic import ValidationError

from app.models import (
    ContentScoringRequest, 
    ContentScoringResponse, 
    KeyPoint, 
    ErrorResponse, 
    HealthResponse
)

class TestKeyPoint:
    """Test cases for KeyPoint model"""
    
    def test_valid_key_point(self):
        """Test valid KeyPoint creation"""
        kp = KeyPoint(point="machine learning", present=True)
        
        assert kp.point == "machine learning"
        assert kp.present is True
    
    def test_key_point_false(self):
        """Test KeyPoint with present=False"""
        kp = KeyPoint(point="artificial intelligence", present=False)
        
        assert kp.point == "artificial intelligence"
        assert kp.present is False

class TestContentScoringRequest:
    """Test cases for ContentScoringRequest model"""
    
    def test_valid_request(self):
        """Test valid request creation"""
        request = ContentScoringRequest(
            question="What is machine learning?",
            answer="Machine learning is a subset of artificial intelligence."
        )
        
        assert request.question == "What is machine learning?"
        assert request.answer == "Machine learning is a subset of artificial intelligence."
    
    def test_question_validation_empty_string(self):
        """Test question validation with empty string"""
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringRequest(
                question="",
                answer="Valid answer"
            )
        
        assert "ensure this value has at least 1 characters" in str(exc_info.value)
    
    def test_question_validation_whitespace_only(self):
        """Test question validation with whitespace only"""
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringRequest(
                question="   \n\t   ",
                answer="Valid answer"
            )
        
        assert "Question cannot be empty" in str(exc_info.value)
    
    def test_question_validation_too_long(self):
        """Test question validation when too long"""
        long_question = "a" * 513  # Exceeds 512 character limit
        
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringRequest(
                question=long_question,
                answer="Valid answer"
            )
        
        assert "Question cannot exceed 512 characters" in str(exc_info.value)
    
    def test_answer_validation_empty_string(self):
        """Test answer validation with empty string"""
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringRequest(
                question="Valid question?",
                answer=""
            )
        
        assert "ensure this value has at least 1 characters" in str(exc_info.value)
    
    def test_answer_validation_whitespace_only(self):
        """Test answer validation with whitespace only"""
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringRequest(
                question="Valid question?",
                answer="   \n\t   "
            )
        
        assert "Answer cannot be empty" in str(exc_info.value)
    
    def test_answer_validation_too_long(self):
        """Test answer validation when too long"""
        long_answer = "a" * 1025  # Exceeds 1024 character limit
        
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringRequest(
                question="Valid question?",
                answer=long_answer
            )
        
        assert "Answer cannot exceed 1024 characters" in str(exc_info.value)
    
    def test_whitespace_normalization_question(self):
        """Test whitespace normalization in question"""
        request = ContentScoringRequest(
            question="  What    is   machine\n\tlearning?  ",
            answer="Valid answer"
        )
        
        assert request.question == "What is machine learning?"
    
    def test_whitespace_normalization_answer(self):
        """Test whitespace normalization in answer"""
        request = ContentScoringRequest(
            question="Valid question?",
            answer="  Machine   learning\n\tis  AI  subset.  "
        )
        
        assert request.answer == "Machine learning is AI subset."
    
    def test_question_exactly_512_chars(self):
        """Test question with exactly 512 characters"""
        question_512 = "a" * 512
        
        request = ContentScoringRequest(
            question=question_512,
            answer="Valid answer"
        )
        
        assert len(request.question) == 512
        assert request.question == question_512
    
    def test_answer_exactly_1024_chars(self):
        """Test answer with exactly 1024 characters"""
        answer_1024 = "a" * 1024
        
        request = ContentScoringRequest(
            question="Valid question?",
            answer=answer_1024
        )
        
        assert len(request.answer) == 1024
        assert request.answer == answer_1024

class TestContentScoringResponse:
    """Test cases for ContentScoringResponse model"""
    
    def test_valid_response(self):
        """Test valid response creation"""
        key_points = [
            KeyPoint(point="machine learning", present=True),
            KeyPoint(point="artificial intelligence", present=False)
        ]
        
        response = ContentScoringResponse(
            success=True,
            score=85.5,
            similarity=0.855,
            key_points=key_points
        )
        
        assert response.success is True
        assert response.score == 85.5
        assert response.similarity == 0.855
        assert len(response.key_points) == 2
        assert response.key_points[0].point == "machine learning"
        assert response.key_points[0].present is True
    
    def test_score_validation_negative(self):
        """Test score validation with negative value"""
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringResponse(
                success=True,
                score=-1.0,
                similarity=0.5,
                key_points=[]
            )
        
        assert "ensure this value is greater than or equal to 0" in str(exc_info.value)
    
    def test_score_validation_too_high(self):
        """Test score validation with value > 100"""
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringResponse(
                success=True,
                score=101.0,
                similarity=0.5,
                key_points=[]
            )
        
        assert "ensure this value is less than or equal to 100" in str(exc_info.value)
    
    def test_similarity_validation_negative(self):
        """Test similarity validation with negative value"""
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringResponse(
                success=True,
                score=50.0,
                similarity=-0.1,
                key_points=[]
            )
        
        assert "ensure this value is greater than or equal to 0" in str(exc_info.value)
    
    def test_similarity_validation_too_high(self):
        """Test similarity validation with value > 1"""
        with pytest.raises(ValidationError) as exc_info:
            ContentScoringResponse(
                success=True,
                score=50.0,
                similarity=1.1,
                key_points=[]
            )
        
        assert "ensure this value is less than or equal to 1" in str(exc_info.value)
    
    def test_score_boundary_values(self):
        """Test score boundary values (0 and 100)"""
        # Test score = 0
        response1 = ContentScoringResponse(
            success=True,
            score=0.0,
            similarity=0.0,
            key_points=[]
        )
        assert response1.score == 0.0
        
        # Test score = 100
        response2 = ContentScoringResponse(
            success=True,
            score=100.0,
            similarity=1.0,
            key_points=[]
        )
        assert response2.score == 100.0
    
    def test_similarity_boundary_values(self):
        """Test similarity boundary values (0 and 1)"""
        # Test similarity = 0
        response1 = ContentScoringResponse(
            success=True,
            score=0.0,
            similarity=0.0,
            key_points=[]
        )
        assert response1.similarity == 0.0
        
        # Test similarity = 1
        response2 = ContentScoringResponse(
            success=True,
            score=100.0,
            similarity=1.0,
            key_points=[]
        )
        assert response2.similarity == 1.0
    
    def test_empty_key_points_list(self):
        """Test response with empty key points list"""
        response = ContentScoringResponse(
            success=True,
            score=75.0,
            similarity=0.75,
            key_points=[]
        )
        
        assert len(response.key_points) == 0
    
    def test_multiple_key_points(self):
        """Test response with multiple key points"""
        key_points = [
            KeyPoint(point="ai", present=True),
            KeyPoint(point="machine learning", present=True),
            KeyPoint(point="neural networks", present=False),
            KeyPoint(point="deep learning", present=True),
        ]
        
        response = ContentScoringResponse(
            success=True,
            score=88.5,
            similarity=0.885,
            key_points=key_points
        )
        
        assert len(response.key_points) == 4
        assert sum(1 for kp in response.key_points if kp.present) == 3

class TestErrorResponse:
    """Test cases for ErrorResponse model"""
    
    def test_valid_error_response(self):
        """Test valid error response creation"""
        error = ErrorResponse(error="Question cannot exceed 512 characters")
        
        assert error.error == "Question cannot exceed 512 characters"
    
    def test_empty_error_message(self):
        """Test error response with empty message"""
        error = ErrorResponse(error="")
        
        assert error.error == ""

class TestHealthResponse:
    """Test cases for HealthResponse model"""
    
    def test_valid_health_response(self):
        """Test valid health response creation"""
        health = HealthResponse(
            status="healthy",
            service="content-scoring",
            version="1.0.0"
        )
        
        assert health.status == "healthy"
        assert health.service == "content-scoring"
        assert health.version == "1.0.0"

class TestModelSerialization:
    """Test cases for model serialization/deserialization"""
    
    def test_content_scoring_request_json(self):
        """Test ContentScoringRequest JSON serialization"""
        request = ContentScoringRequest(
            question="What is AI?",
            answer="AI is artificial intelligence."
        )
        
        json_data = request.model_dump()
        
        assert json_data["question"] == "What is AI?"
        assert json_data["answer"] == "AI is artificial intelligence."
    
    def test_content_scoring_response_json(self):
        """Test ContentScoringResponse JSON serialization"""
        key_points = [KeyPoint(point="ai", present=True)]
        response = ContentScoringResponse(
            success=True,
            score=90.0,
            similarity=0.9,
            key_points=key_points
        )
        
        json_data = response.model_dump()
        
        assert json_data["success"] is True
        assert json_data["score"] == 90.0
        assert json_data["similarity"] == 0.9
        assert len(json_data["key_points"]) == 1
        assert json_data["key_points"][0]["point"] == "ai"
        assert json_data["key_points"][0]["present"] is True
    
    def test_model_validation_error_details(self):
        """Test validation error details"""
        try:
            ContentScoringRequest(
                question="",  # Empty question
                answer="a" * 1025  # Too long answer
            )
        except ValidationError as e:
            errors = e.errors()
            
            # Should have validation errors for both fields
            assert len(errors) >= 2
            
            # Check that we have errors for both question and answer
            error_fields = {error['loc'][0] for error in errors}
            assert 'question' in error_fields
            assert 'answer' in error_fields