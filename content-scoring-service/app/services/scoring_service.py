"""
Content Scoring Service
Implements the core logic for scoring content similarity between questions and answers
"""

import re
import string
import logging
from typing import List, Dict, Tuple
import asyncio
from functools import lru_cache

import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import spacy
from collections import Counter

from app.models import ContentScoringResponse, KeyPoint

logger = logging.getLogger(__name__)

class ContentScoringService:
    """Service class for content scoring operations"""
    
    def __init__(self):
        """Initialize the scoring service with required models"""
        logger.info("Initializing Content Scoring Service...")
        
        # Load sentence transformer model
        try:
            self.sentence_model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')
            logger.info("Sentence transformer model loaded successfully")
        except Exception as e:
            logger.error(f"Failed to load sentence transformer: {e}")
            raise
        
        # Load spaCy model for NLP tasks
        try:
            self.nlp = spacy.load("en_core_web_sm")
            logger.info("spaCy model loaded successfully")
        except OSError:
            logger.warning("spaCy en_core_web_sm model not found, using basic processing")
            self.nlp = None
        
        # Cache for embeddings to improve performance
        self._embedding_cache = {}
        
        logger.info("Content Scoring Service initialized successfully")
    
    def preprocess_text(self, text: str) -> str:
        """
        Preprocess text by cleaning and normalizing
        
        Args:
            text: Raw text string
            
        Returns:
            Cleaned and normalized text
        """
        # Convert to lowercase
        text = text.lower().strip()
        
        # Remove extra whitespace
        text = re.sub(r'\s+', ' ', text)
        
        # Remove punctuation (but keep apostrophes for contractions)
        text = re.sub(r'[^\w\s\']', '', text)
        
        # Remove extra spaces again after punctuation removal
        text = re.sub(r'\s+', ' ', text).strip()
        
        return text
    
    @lru_cache(maxsize=1000)
    def get_embedding(self, text: str) -> np.ndarray:
        """
        Get sentence embedding with caching
        
        Args:
            text: Text to encode
            
        Returns:
            Sentence embedding as numpy array
        """
        try:
            embedding = self.sentence_model.encode([text])
            return embedding[0]
        except Exception as e:
            logger.error(f"Failed to get embedding: {e}")
            # Return zero vector as fallback
            return np.zeros(384)  # all-MiniLM-L6-v2 has 384 dimensions
    
    def calculate_similarity(self, question: str, answer: str) -> float:
        """
        Calculate cosine similarity between question and answer
        
        Args:
            question: Question text
            answer: Answer text
            
        Returns:
            Cosine similarity score (0-1)
        """
        try:
            # Preprocess texts
            question_clean = self.preprocess_text(question)
            answer_clean = self.preprocess_text(answer)
            
            # Get embeddings
            question_embedding = self.get_embedding(question_clean)
            answer_embedding = self.get_embedding(answer_clean)
            
            # Calculate cosine similarity
            similarity = cosine_similarity(
                question_embedding.reshape(1, -1),
                answer_embedding.reshape(1, -1)
            )[0][0]
            
            # Ensure similarity is between 0 and 1
            similarity = max(0.0, min(1.0, similarity))
            
            return float(similarity)
            
        except Exception as e:
            logger.error(f"Failed to calculate similarity: {e}")
            return 0.0
    
    def extract_key_points(self, question: str) -> List[str]:
        """
        Extract key points/keywords from the question
        
        Args:
            question: Question text
            
        Returns:
            List of key points/keywords
        """
        key_points = []
        
        try:
            # Preprocess question
            question_clean = self.preprocess_text(question)
            
            if self.nlp:
                # Use spaCy for advanced NLP
                doc = self.nlp(question_clean)
                
                # Extract named entities
                for ent in doc.ents:
                    if len(ent.text) > 2:  # Skip very short entities
                        key_points.append(ent.text.lower())
                
                # Extract important nouns and adjectives
                for token in doc:
                    if (token.pos_ in ['NOUN', 'ADJ'] and 
                        not token.is_stop and 
                        not token.is_punct and 
                        len(token.text) > 2):
                        key_points.append(token.lemma_.lower())
                
                # Extract noun phrases
                for chunk in doc.noun_chunks:
                    if len(chunk.text) > 3 and len(chunk.text.split()) <= 3:
                        key_points.append(chunk.text.lower())
            
            else:
                # Fallback: simple keyword extraction using word frequency
                words = question_clean.split()
                # Remove common stop words
                stop_words = {
                    'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
                    'of', 'with', 'by', 'from', 'is', 'are', 'was', 'were', 'be', 'been',
                    'have', 'has', 'had', 'do', 'does', 'did', 'will', 'would', 'could',
                    'should', 'what', 'when', 'where', 'why', 'how', 'which', 'who'
                }
                
                for word in words:
                    if (len(word) > 2 and 
                        word not in stop_words and 
                        word.isalpha()):
                        key_points.append(word)
                
                # Add bigrams for better context
                for i in range(len(words) - 1):
                    bigram = f"{words[i]} {words[i+1]}"
                    if (len(bigram) > 5 and 
                        words[i] not in stop_words and 
                        words[i+1] not in stop_words):
                        key_points.append(bigram)
            
            # Remove duplicates and sort by frequency
            key_point_counts = Counter(key_points)
            
            # Return top key points (max 10)
            top_key_points = [point for point, count in key_point_counts.most_common(10)]
            
            return top_key_points
            
        except Exception as e:
            logger.error(f"Failed to extract key points: {e}")
            # Fallback: simple word splitting
            words = self.preprocess_text(question).split()
            return [word for word in words if len(word) > 2][:5]
    
    def check_key_points_presence(self, key_points: List[str], answer: str) -> List[KeyPoint]:
        """
        Check which key points are present in the answer
        
        Args:
            key_points: List of key points to check
            answer: Answer text to search in
            
        Returns:
            List of KeyPoint objects with presence information
        """
        answer_clean = self.preprocess_text(answer)
        result = []
        
        for point in key_points:
            # Check if key point is present (case-insensitive, partial matching)
            present = point.lower() in answer_clean.lower()
            
            # For multi-word key points, check if all words are present
            if not present and ' ' in point:
                point_words = point.split()
                present = all(word in answer_clean for word in point_words)
            
            result.append(KeyPoint(point=point, present=present))
        
        return result
    
    async def score_content(self, question: str, answer: str) -> ContentScoringResponse:
        """
        Score content similarity between question and answer
        
        Args:
            question: Question text
            answer: Answer text
            
        Returns:
            ContentScoringResponse with scoring results
        """
        try:
            logger.info("Starting content scoring process")
            
            # Calculate similarity using sentence transformers
            similarity = self.calculate_similarity(question, answer)
            
            # Convert similarity to score (0-100)
            score = round(similarity * 100, 2)
            
            # Extract key points from question
            key_points = self.extract_key_points(question)
            
            # Check key points presence in answer
            key_point_results = self.check_key_points_presence(key_points, answer)
            
            # Adjust score based on key points presence
            if key_point_results:
                present_count = sum(1 for kp in key_point_results if kp.present)
                key_point_bonus = (present_count / len(key_point_results)) * 0.1  # Max 10% bonus
                score = min(100.0, score + (key_point_bonus * 100))
                score = round(score, 2)
            
            logger.info(f"Content scoring completed: score={score}, similarity={similarity}")
            
            return ContentScoringResponse(
                success=True,
                score=score,
                similarity=round(similarity, 3),
                key_points=key_point_results
            )
            
        except Exception as e:
            logger.error(f"Failed to score content: {e}")
            raise ValueError(f"Content scoring failed: {str(e)}")