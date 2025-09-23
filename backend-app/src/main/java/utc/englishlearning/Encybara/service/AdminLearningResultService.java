package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.Learning_Result;
import utc.englishlearning.Encybara.domain.response.admin.AdminLearningResultDTO;
import utc.englishlearning.Encybara.repository.LearningResultRepository;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;

@Service
public class AdminLearningResultService {

    @Autowired
    private LearningResultRepository learningResultRepository;

    private AdminLearningResultDTO convertToDTO(Learning_Result result) {
        AdminLearningResultDTO dto = new AdminLearningResultDTO();
        dto.setId(result.getId());
        dto.setUserId(result.getUser().getId());
        dto.setUserName(result.getUser().getName());
        dto.setUserEmail(result.getUser().getEmail());
        dto.setEnglishLevel(result.getUser().getEnglishlevel());

        // Current scores
        dto.setListeningScore(result.getListeningScore());
        dto.setSpeakingScore(result.getSpeakingScore());
        dto.setReadingScore(result.getReadingScore());
        dto.setWritingScore(result.getWritingScore());

        // Previous scores
        dto.setPreviousListeningScore(result.getPreviousListeningScore());
        dto.setPreviousSpeakingScore(result.getPreviousSpeakingScore());
        dto.setPreviousReadingScore(result.getPreviousReadingScore());
        dto.setPreviousWritingScore(result.getPreviousWritingScore());

        dto.setLastUpdated(result.getLastUpdated());

        // Calculate progress
        dto.setListeningProgress(result.getListeningScore() - result.getPreviousListeningScore());
        dto.setSpeakingProgress(result.getSpeakingScore() - result.getPreviousSpeakingScore());
        dto.setReadingProgress(result.getReadingScore() - result.getPreviousReadingScore());
        dto.setWritingProgress(result.getWritingScore() - result.getPreviousWritingScore());

        // Calculate overall progress
        double overallProgress = (dto.getListeningProgress() +
                dto.getSpeakingProgress() +
                dto.getReadingProgress() +
                dto.getWritingProgress()) / 4.0;
        dto.setOverallProgress(overallProgress);

        return dto;
    }

    public Page<AdminLearningResultDTO> getAllLearningResults(Pageable pageable) {
        return learningResultRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public AdminLearningResultDTO getLearningResultById(Long id) {
        Learning_Result result = learningResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning result not found with ID: " + id));
        return convertToDTO(result);
    }

    public Page<AdminLearningResultDTO> searchLearningResults(
            String userEmail,
            String userName,
            Pageable pageable) {
        return learningResultRepository.searchByFilters(userEmail, userName, pageable)
                .map(this::convertToDTO);
    }

    public Page<AdminLearningResultDTO> getLearningResultsByLevel(String level, Pageable pageable) {
        return learningResultRepository.findByUserEnglishlevel(level, pageable)
                .map(this::convertToDTO);
    }
}