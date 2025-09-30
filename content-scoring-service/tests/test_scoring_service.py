"""
Test cases for ContentScoringService
"""

import pytest
import numpy as np
from unittest.mock import Mock, patch, MagicMock

from app.services.scoring_service import ContentScoringService
from app.models import KeyPoint, AdvancedAnswer

class TestContentScoringService:
    """Test cases for ContentScoringService class"""
    
    @pytest.fixture
    def scoring_service(self):
        """Create a ContentScoringService instance for testing"""
        with patch('app.services.scoring_service.SentenceTransformer') as mock_st, \
             patch('app.services.scoring_service.spacy.load') as mock_spacy:
            
            # Mock sentence transformer
            mock_model = Mock()
            mock_model.encode.return_value = np.array([[0.1, 0.2, 0.3] * 128])  # 384 dimensions
            mock_st.return_value = mock_model
            
            # Mock spaCy
            mock_nlp = Mock()
            mock_spacy.return_value = mock_nlp
            
            service = ContentScoringService()
            service.sentence_model = mock_model
            service.nlp = mock_nlp
            
            return service
    
    def test_preprocess_text(self, scoring_service):
        """Test text preprocessing functionality"""
        # Test basic preprocessing
        input_text = "  Hello World!  What's up?  "
        expected = "hello world whats up"
        result = scoring_service.preprocess_text(input_text)
        assert result == expected
        
        # Test with special characters
        input_text = "AI & ML... (2023)!!!"
        expected = "ai ml 2023"
        result = scoring_service.preprocess_text(input_text)
        assert result == expected
        
        # Test with numbers and punctuation
        input_text = "Python 3.9 is great!"
        expected = "python 39 is great"
        result = scoring_service.preprocess_text(input_text)
        assert result == expected
    
    def test_get_embedding(self, scoring_service):
        """Test embedding generation with caching"""
        text = "test text"
        
        # First call should use the model
        embedding1 = scoring_service.get_embedding(text)
        assert isinstance(embedding1, np.ndarray)
        
        # Second call should use cache (same result)
        embedding2 = scoring_service.get_embedding(text)
        assert np.array_equal(embedding1, embedding2)
        
        # Verify the model was called
        scoring_service.sentence_model.encode.assert_called()
    
    def test_get_embedding_error_handling(self, scoring_service):
        """Test embedding generation error handling"""
        # Mock the sentence model to raise an exception
        scoring_service.sentence_model.encode.side_effect = Exception("Model error")
        
        text = "test text"
        embedding = scoring_service.get_embedding(text)
        
        # Should return zero vector as fallback
        assert isinstance(embedding, np.ndarray)
        assert embedding.shape == (384,)  # all-MiniLM-L6-v2 dimensions
        assert np.all(embedding == 0)
    
    @patch('app.services.scoring_service.cosine_similarity')
    def test_calculate_similarity(self, mock_cosine, scoring_service):
        """Test similarity calculation"""
        # Mock cosine similarity result
        mock_cosine.return_value = np.array([[0.85]])
        
        question = "What is machine learning?"
        answer = "Machine learning is a type of AI"
        
        similarity = scoring_service.calculate_similarity(question, answer)
        
        assert similarity == 0.85
        mock_cosine.assert_called_once()
    
    @patch('app.services.scoring_service.cosine_similarity')
    def test_calculate_similarity_bounds(self, mock_cosine, scoring_service):
        """Test similarity calculation bounds checking"""
        # Test upper bound
        mock_cosine.return_value = np.array([[1.1]])  # Above 1.0
        result = scoring_service.calculate_similarity("test", "test")
        assert result == 1.0
        
        # Test lower bound
        mock_cosine.return_value = np.array([[-0.1]])  # Below 0.0
        result = scoring_service.calculate_similarity("test", "different")
        assert result == 0.0
    
    def test_extract_key_points_with_spacy(self, scoring_service):
        """Test key point extraction with spaCy"""
        # Mock spaCy components
        mock_doc = Mock()
        
        # Mock entities
        mock_entity = Mock()
        mock_entity.text = "Machine Learning"
        mock_doc.ents = [mock_entity]
        
        # Mock tokens
        mock_token1 = Mock()
        mock_token1.pos_ = "NOUN"
        mock_token1.is_stop = False
        mock_token1.is_punct = False
        mock_token1.text = "algorithm"
        mock_token1.lemma_ = "algorithm"
        
        mock_token2 = Mock()
        mock_token2.pos_ = "ADJ"
        mock_token2.is_stop = False
        mock_token2.is_punct = False
        mock_token2.text = "intelligent"
        mock_token2.lemma_ = "intelligent"
        
        mock_doc.__iter__ = Mock(return_value=iter([mock_token1, mock_token2]))
        
        # Mock noun chunks
        mock_chunk = Mock()
        mock_chunk.text = "artificial intelligence"
        mock_doc.noun_chunks = [mock_chunk]
        
        scoring_service.nlp.return_value = mock_doc
        
        question = "What is machine learning algorithm for artificial intelligence?"
        key_points = scoring_service.extract_key_points(question)
        
        assert isinstance(key_points, list)
        assert len(key_points) > 0
    
    def test_extract_key_points_fallback(self, scoring_service):
        """Test key point extraction fallback without spaCy"""
        # Set nlp to None to trigger fallback
        scoring_service.nlp = None
        
        question = "What is machine learning and artificial intelligence?"
        key_points = scoring_service.extract_key_points(question)
        
        assert isinstance(key_points, list)
        # Should extract words longer than 2 characters
        assert any(len(point) > 2 for point in key_points)
    
    def test_extract_key_points_error_handling(self, scoring_service):
        """Test key point extraction error handling"""
        # Mock spaCy to raise an exception
        scoring_service.nlp.side_effect = Exception("spaCy error")
        
        question = "What is machine learning?"
        key_points = scoring_service.extract_key_points(question)
        
        # Should fallback to simple word splitting
        assert isinstance(key_points, list)
        assert len(key_points) <= 5  # Limited to 5 in fallback
    
    def test_check_key_points_presence(self, scoring_service):
        """Test key points presence checking"""
        key_points = ["machine learning", "artificial", "intelligence"]
        answer = "Machine learning is a subset of artificial intelligence that uses algorithms."
        
        results = scoring_service.check_key_points_presence(key_points, answer)
        
        assert len(results) == 3
        assert all(isinstance(result, KeyPoint) for result in results)
        
        # Check specific results
        ml_result = next((r for r in results if r.point == "machine learning"), None)
        assert ml_result is not None
        assert ml_result.present is True
        
        ai_result = next((r for r in results if r.point == "artificial"), None)
        assert ai_result is not None
        assert ai_result.present is True
    
    def test_check_key_points_multi_word(self, scoring_service):
        """Test key points presence checking for multi-word phrases"""
        key_points = ["deep learning", "neural network"]
        answer = "Deep learning uses neural networks for pattern recognition."

        results = scoring_service.check_key_points_presence(key_points, answer)

        assert len(results) == 2

        # Both multi-word key points should be found
        dl_result = next((r for r in results if r.point == "deep learning"), None)
        assert dl_result is not None
        assert dl_result.present is True

        nn_result = next((r for r in results if r.point == "neural network"), None)
        assert nn_result is not None
        assert nn_result.present is True

    def test_generate_advanced_answer(self, scoring_service):
        """Test advanced answer generation"""
        question = "What is machine learning?"
        answer = "It is AI."
        key_points = ["machine learning", "algorithms", "data"]
        key_point_results = [
            KeyPoint(point="machine learning", present=False),
            KeyPoint(point="algorithms", present=False),
            KeyPoint(point="data", present=False)
        ]

        result = scoring_service.generate_advanced_answer(
            question, answer, key_points, key_point_results
        )

        assert isinstance(result, AdvancedAnswer)
        assert result.suggestion != ""
        assert len(result.improvement_points) > 0
        assert len(result.missing_concepts) > 0
        assert "machine learning" in result.missing_concepts

    def test_generate_advanced_answer_with_good_answer(self, scoring_service):
        """Test advanced answer generation with good answer"""
        question = "What is machine learning?"
        answer = "Machine learning is a subset of artificial intelligence that enables systems to learn and improve from experience without being explicitly programmed. It uses algorithms to analyze data patterns."
        key_points = ["machine learning", "algorithms", "data"]
        key_point_results = [
            KeyPoint(point="machine learning", present=True),
            KeyPoint(point="algorithms", present=True),
            KeyPoint(point="data", present=True)
        ]

        result = scoring_service.generate_advanced_answer(
            question, answer, key_points, key_point_results
        )

        assert isinstance(result, AdvancedAnswer)
        assert result.suggestion != ""
        # Should have fewer improvement points for good answer
        assert len(result.missing_concepts) == 0
    
    @pytest.mark.asyncio
    async def test_score_content_success(self, scoring_service):
        """Test successful content scoring"""
        # Mock the methods used in score_content
        with patch.object(scoring_service, 'calculate_similarity', return_value=0.85), \
             patch.object(scoring_service, 'extract_key_points', return_value=["ai", "ml"]), \
             patch.object(scoring_service, 'check_key_points_presence') as mock_check, \
             patch.object(scoring_service, 'generate_advanced_answer') as mock_advanced:

            # Mock key points results
            mock_check.return_value = [
                KeyPoint(point="ai", present=True),
                KeyPoint(point="ml", present=False)
            ]

            # Mock advanced answer
            mock_advanced.return_value = AdvancedAnswer(
                suggestion="AI and ML are fundamental concepts...",
                improvement_points=["Add more detail"],
                missing_concepts=["ml"]
            )

            question = "What is AI and ML?"
            answer = "AI is artificial intelligence used in technology."

            result = await scoring_service.score_content(question, answer)

            assert result.success is True
            assert result.score > 0
            assert result.similarity == 0.85
            assert len(result.key_points) == 2
            assert result.advanced_answer is not None
            assert result.advanced_answer.suggestion != ""
    
    @pytest.mark.asyncio
    async def test_score_content_with_key_point_bonus(self, scoring_service):
        """Test content scoring with key point bonus"""
        with patch.object(scoring_service, 'calculate_similarity', return_value=0.80), \
             patch.object(scoring_service, 'extract_key_points', return_value=["ai", "ml"]), \
             patch.object(scoring_service, 'check_key_points_presence') as mock_check, \
             patch.object(scoring_service, 'generate_advanced_answer') as mock_advanced:

            # All key points present - should get bonus
            mock_check.return_value = [
                KeyPoint(point="ai", present=True),
                KeyPoint(point="ml", present=True)
            ]

            # Mock advanced answer
            mock_advanced.return_value = AdvancedAnswer(
                suggestion="Complete answer...",
                improvement_points=[],
                missing_concepts=[]
            )

            question = "What is AI and ML?"
            answer = "AI and ML are related technologies."

            result = await scoring_service.score_content(question, answer)

            # Score should be higher than base similarity due to key point bonus
            assert result.score > 80.0  # Base similarity * 100
            assert result.score <= 100.0  # But capped at 100
    
    @pytest.mark.asyncio
    async def test_score_content_error_handling(self, scoring_service):
        """Test content scoring error handling"""
        # Mock calculate_similarity to raise an exception
        with patch.object(scoring_service, 'calculate_similarity', side_effect=Exception("Calculation error")):
            
            question = "What is AI?"
            answer = "AI is artificial intelligence."
            
            with pytest.raises(ValueError, match="Content scoring failed"):
                await scoring_service.score_content(question, answer)

class TestIntegration:
    """Integration tests for ContentScoringService"""
    
    def test_service_initialization_without_spacy(self):
        """Test service initialization when spaCy model is not available"""
        with patch('app.services.scoring_service.SentenceTransformer') as mock_st, \
             patch('app.services.scoring_service.spacy.load', side_effect=OSError("Model not found")):
            
            mock_model = Mock()
            mock_st.return_value = mock_model
            
            # Should initialize successfully without spaCy
            service = ContentScoringService()
            assert service.nlp is None
            assert service.sentence_model is not None
    
    def test_service_initialization_sentence_transformer_error(self):
        """Test service initialization when SentenceTransformer fails"""
        with patch('app.services.scoring_service.SentenceTransformer', side_effect=Exception("Model load failed")):
            
            with pytest.raises(Exception):
                ContentScoringService()