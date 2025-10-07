"""Ultra-lightweight content scoring service."""

from fastapi import FastAPI
from pydantic import BaseModel
import re
from typing import Dict

app = FastAPI(title="Content Scoring API - Lightweight")

class ScoringRequest(BaseModel):
    question: str
    answer: str

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "content-scoring-lightweight"}

@app.get("/")
def root():
    return {"message": "Content Scoring Service - Lightweight Version"}

@app.post("/api/content-scoring")
def score_content(request: ScoringRequest) -> Dict:
    """Simple content scoring using text similarity."""
    try:
        # Simple text similarity scoring
        question_words = set(re.findall(r"\w+", request.question.lower()))
        answer_words = set(re.findall(r"\w+", request.answer.lower()))
        
        # Calculate overlap
        overlap = len(question_words.intersection(answer_words))
        total_words = len(question_words.union(answer_words))
        
        if total_words == 0:
            similarity = 0.0
        else:
            similarity = overlap / total_words
            
        # Calculate scores
        relevance_score = min(100, similarity * 150)
        completeness_score = min(100, len(answer_words) * 10)
        overall_score = (relevance_score + completeness_score) / 2
        
        return {
            "score": round(overall_score, 2),
            "relevance_score": round(relevance_score, 2), 
            "completeness_score": round(completeness_score, 2),
            "feedback": "Content analyzed using lightweight text similarity.",
            "method": "simple-similarity"
        }
        
    except Exception as e:
        return {
            "score": 50.0,
            "error": str(e),
            "method": "simple-similarity"
        }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5001)