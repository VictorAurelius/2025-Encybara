package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.Flashcard;
import utc.englishlearning.Encybara.domain.FlashcardGroup;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.domain.response.dictionary.Definition;
import utc.englishlearning.Encybara.domain.response.dictionary.Meaning;
import utc.englishlearning.Encybara.domain.response.dictionary.Phonetic;
import utc.englishlearning.Encybara.domain.response.dictionary.ResWord;
import utc.englishlearning.Encybara.domain.response.flashcard.ResFlashcardDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.FlashcardRepository;
import utc.englishlearning.Encybara.repository.FlashcardGroupRepository;
import utc.englishlearning.Encybara.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.lang.StringBuilder;

@Service
public class FlashcardService {

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardGroupRepository flashcardGroupRepository;

    @Autowired
    private GoogleTranslateService googleTranslateService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private UserRepository userRepository;

    public ResFlashcardDTO createFlashcard(String word, List<Integer> definitionIndices, List<Integer> meaningIndices,
            List<Integer> phoneticIndices, Long userId) {
        Flashcard flashcard = new Flashcard();
        flashcard.setWord(word);
        flashcard.setLearnedStatus(false);
        flashcard.setAddedDate(Instant.now());
        flashcard.setLastReviewed(flashcard.getAddedDate());

        // Tìm User từ userId
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        flashcard.setUser(user); // Gán đối tượng User

        // Sử dụng GoogleTranslateService để dịch
        String vietnameseMeaning = googleTranslateService.translate(word, "vi").block();
        flashcard.setVietNameseMeaning(vietnameseMeaning);

        // Sử dụng DictionaryService để lấy các thuộc tính khác
        List<ResWord> definitions = dictionaryService.getWordDefinition(word).block();

        // Lưu trữ định nghĩa, ví dụ, và phần bài phát biểu đã chọn
        StringBuilder selectedDefinitions = new StringBuilder();
        StringBuilder selectedExamples = new StringBuilder();
        StringBuilder selectedPartOfSpeech = new StringBuilder();

        for (int index : definitionIndices) {
            if (index < definitions.size()) {
                ResWord definition = definitions.get(index);
                // Lấy phần bài phát biểu và định nghĩa
                for (Meaning meaning : definition.getMeanings()) {
                    selectedPartOfSpeech.append(meaning.getPartOfSpeech()).append("; ");
                    for (Definition def : meaning.getDefinitions()) {
                        selectedDefinitions.append(def.getDefinition()).append("; ");
                        // Lấy ví dụ nếu có
                        if (def.getExample() != null) {
                            selectedExamples.append(def.getExample()).append("; ");
                        }
                        // Chỉ cần lấy một định nghĩa
                        break; // Dừng lại sau khi lấy định nghĩa đầu tiên
                    }
                }
            } else {
                System.out.println("Invalid definition index: " + index);
            }
        }

        // Lưu phonetics đã chọn
        StringBuilder selectedPhoneticsText = new StringBuilder();
        StringBuilder selectedPhoneticsAudio = new StringBuilder();

        for (int index : phoneticIndices) {
            if (index < definitions.size()) {
                ResWord definition = definitions.get(index);
                for (Phonetic phonetic : definition.getPhonetics()) {
                    selectedPhoneticsText.append(phonetic.getText()).append("; ");
                    selectedPhoneticsAudio.append(phonetic.getAudio()).append("; ");
                }
            }
        }

        flashcard.setDefinitions(selectedDefinitions.toString());
        flashcard.setExamples(selectedExamples.toString());
        flashcard.setPartOfSpeech(selectedPartOfSpeech.toString());
        flashcard.setPhoneticText(selectedPhoneticsText.toString());
        flashcard.setPhoneticAudio(selectedPhoneticsAudio.toString());

        FlashcardGroup allFlashcardsGroup = flashcardGroupRepository.findByName("New Flashcards");
        if (allFlashcardsGroup == null) {
            allFlashcardsGroup = new FlashcardGroup();
            allFlashcardsGroup.setName("New Flashcards");
            allFlashcardsGroup.setUser(user); // Gán đối tượng User cho nhóm
            flashcardGroupRepository.save(allFlashcardsGroup);
        }
        flashcard.setFlashcardGroup(allFlashcardsGroup);

        flashcardRepository.save(flashcard);

        ResFlashcardDTO res = new ResFlashcardDTO();
        res.setId(flashcard.getId());
        res.setWord(flashcard.getWord());
        res.setVietNameseMeaning(flashcard.getVietNameseMeaning());
        res.setDefinitions(flashcard.getDefinitions());
        res.setExamples(flashcard.getExamples());
        res.setPartOfSpeech(flashcard.getPartOfSpeech());
        res.setPhoneticText(flashcard.getPhoneticText());
        res.setPhoneticAudio(flashcard.getPhoneticAudio());
        res.setUserId(flashcard.getUser().getId());
        res.setAddedDate(flashcard.getAddedDate());
        res.setLearnedStatus(flashcard.isLearnedStatus());
        res.setLastReviewed(flashcard.getLastReviewed());

        return res;
    }

    public void deleteFlashcard(Long flashcardId) {
        if (!flashcardRepository.existsById(flashcardId)) {
            throw new ResourceNotFoundException("Flashcard not found");
        }
        flashcardRepository.deleteById(flashcardId);
    }

    public void markFlashcardAsLearned(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
        flashcard.setLearnedStatus(true);
        flashcardRepository.save(flashcard);
    }

    public ResFlashcardDTO getFlashcard(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        // Cập nhật lastReviewed
        flashcard.setLastReviewed(Instant.now());
        flashcardRepository.save(flashcard); // Lưu lại thay đổi

        ResFlashcardDTO res = new ResFlashcardDTO();
        res.setId(flashcard.getId());
        res.setWord(flashcard.getWord());
        res.setDefinitions(flashcard.getDefinitions());
        res.setExamples(flashcard.getExamples());
        res.setPartOfSpeech(flashcard.getPartOfSpeech());
        res.setPhoneticText(flashcard.getPhoneticText());
        res.setPhoneticAudio(flashcard.getPhoneticAudio());
        res.setVietNameseMeaning(flashcard.getVietNameseMeaning());
        res.setUserId(flashcard.getUser().getId());
        res.setAddedDate(flashcard.getAddedDate());
        res.setLearnedStatus(flashcard.isLearnedStatus());
        res.setLastReviewed(flashcard.getLastReviewed()); // Thêm lastReviewed vào DTO

        return res;
    }

    public Page<Flashcard> getAllFlashcardsSortedByLatest(Pageable pageable) {
        return flashcardRepository.findAllByOrderByLastReviewedDesc(pageable);
    }

    public Page<Flashcard> getAllFlashcardsSortedByOldest(Pageable pageable) {
        return flashcardRepository.findAllByOrderByLastReviewedAsc(pageable);
    }

    public void markFlashcardAsUnlearned(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
        flashcard.setLearnedStatus(false); // Đánh dấu là chưa học
        flashcardRepository.save(flashcard);
    }
}