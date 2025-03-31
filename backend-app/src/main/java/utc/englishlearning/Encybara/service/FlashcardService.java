package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.Flashcard;
import utc.englishlearning.Encybara.domain.FlashcardGroup;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.domain.response.dictionary.Definition;
import utc.englishlearning.Encybara.domain.response.dictionary.GlosbeResponseDTO;
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
import java.io.IOException;

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

    @Autowired
    private GlosbeDictionaryService glosbeDictionaryService;

    @SuppressWarnings("null")
    public ResFlashcardDTO createFlashcardForEnglishDefinition(String word, int partOfSpeechIndex,
            List<Integer> definitionIndices, Long userId) {
        Flashcard flashcard = new Flashcard();
        flashcard.setWord(word);
        flashcard.setLearnedStatus(false);
        flashcard.setAddedDate(Instant.now());
        flashcard.setLastReviewed(flashcard.getAddedDate());

        // Tìm User từ userId
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        flashcard.setUser(user); // Gán đối tượng User

        // Sử dụng DictionaryService để lấy các thuộc tính khác
        List<ResWord> definitions = dictionaryService.getWordDefinition(word).block();

        // Lưu trữ định nghĩa, ví dụ, và phần bài phát biểu đã chọn
        StringBuilder selectedDefinitions = new StringBuilder();
        StringBuilder selectedExamples = new StringBuilder();
        StringBuilder selectedPartOfSpeech = new StringBuilder();

        // Tìm phần bài phát biểu theo chỉ số
        for (ResWord definition : definitions) {
            List<Meaning> meanings = definition.getMeanings();
            if (partOfSpeechIndex < meanings.size()) {
                Meaning selectedMeaning = meanings.get(partOfSpeechIndex);
                selectedPartOfSpeech.append(selectedMeaning.getPartOfSpeech()).append("");

                // Lưu trữ định nghĩa và ví dụ từ phần bài phát biểu đã chọn
                for (int index : definitionIndices) {
                    if (index < selectedMeaning.getDefinitions().size()) {
                        Definition def = selectedMeaning.getDefinitions().get(index);
                        selectedDefinitions.append(def.getDefinition()).append("");

                        if (def.getExample() != null) {
                            selectedExamples.append(def.getExample()).append("");

                            // Dịch ví dụ sang tiếng Việt và lưu vào exampleMeaning
                            String exampleMeaning = googleTranslateService.translate(def.getExample(), "vi").block();
                            flashcard.setExampleMeaning(exampleMeaning);
                        }

                        // Lấy nghĩa tiếng Việt từ định nghĩa đầu tiên
                        String vietnameseMeaning = googleTranslateService.translate(def.getDefinition(), "vi").block();
                        flashcard.setVietNameseMeaning(vietnameseMeaning);
                    } else {
                        System.out.println("Invalid definition index: " + index);
                    }
                }
            } else {
                System.out.println("Invalid part of speech index: " + partOfSpeechIndex);
            }
        }

        // Lưu phonetics đã chọn
        String selectedPhoneticsText = null;
        String selectedPhoneticsAudio = null;

        // Chỉ lấy một phonetic từ phần bài phát biểu đã chọn
        for (ResWord definition : definitions) {
            List<Meaning> meanings = definition.getMeanings();
            if (partOfSpeechIndex < meanings.size()) {
                List<Phonetic> phonetics = definition.getPhonetics();
                if (phonetics != null && !phonetics.isEmpty()) {
                    Phonetic phonetic = phonetics.get(0); // Lấy phonetic đầu tiên
                    if (phonetic.getText() != null) {
                        selectedPhoneticsText = phonetic.getText();
                    }
                    if (phonetic.getAudio() != null) {
                        selectedPhoneticsAudio = phonetic.getAudio();
                    }
                    break; // Chỉ lấy phonetic đầu tiên tìm thấy
                }
            }
        }

        flashcard.setDefinitions(selectedDefinitions.toString());
        flashcard.setExamples(selectedExamples.toString());
        flashcard.setPartOfSpeech(selectedPartOfSpeech.toString());
        flashcard.setPhoneticText(selectedPhoneticsText != null ? selectedPhoneticsText : "");
        flashcard.setPhoneticAudio(selectedPhoneticsAudio != null ? selectedPhoneticsAudio : "");

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
        res.setExampleMeaning(flashcard.getExampleMeaning());

        return res;
    }

    public ResFlashcardDTO createFlashcardForVietNameseMeaning(String word, Long userId, int meaningIndex) {
        Flashcard flashcard = new Flashcard();
        flashcard.setWord(word);
        flashcard.setLearnedStatus(false);
        flashcard.setAddedDate(Instant.now());
        flashcard.setLastReviewed(flashcard.getAddedDate());

        // Tìm User từ userId
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        flashcard.setUser(user); // Gán đối tượng User

        GlosbeResponseDTO glosbeResponse;
        try {
            // Lấy dữ liệu từ GlosbeDictionaryService
            glosbeResponse = glosbeDictionaryService.getWordDefinition(word).block();
        } catch (IOException e) {
            throw new RuntimeException("Error fetching word definition: " + e.getMessage());
        }

        // Kiểm tra xem có dữ liệu không
        if (glosbeResponse == null || glosbeResponse.getMeanings().isEmpty()) {
            throw new RuntimeException("No meanings found for the word: " + word);
        }

        // Lấy meaning dựa trên meaningIndex
        if (meaningIndex < 0 || meaningIndex >= glosbeResponse.getMeanings().size()) {
            throw new RuntimeException("Invalid meaning index: " + meaningIndex);
        }

        GlosbeResponseDTO.Meaning selectedMeaning = glosbeResponse.getMeanings().get(meaningIndex);

        // Lưu thuộc tính flashcard
        flashcard.setVietNameseMeaning(selectedMeaning.getTranslateMeaning());
        flashcard.setPartOfSpeech(selectedMeaning.getPartOfSpeech());
        flashcard.setExamples(selectedMeaning.getExample());
        flashcard.setExampleMeaning(selectedMeaning.getExampleMeaning());

        // Lưu phonetics từ DictionaryService
        try {
            List<ResWord> wordDefinitions = dictionaryService.getWordDefinition(word).block();
            if (wordDefinitions != null && !wordDefinitions.isEmpty()) {
                ResWord definition = wordDefinitions.get(0);
                List<Phonetic> phonetics = definition.getPhonetics();
                if (phonetics != null && !phonetics.isEmpty()) {
                    Phonetic phonetic = phonetics.get(0);
                    if (phonetic.getText() != null) {
                        flashcard.setPhoneticText(phonetic.getText());
                    }
                    if (phonetic.getAudio() != null) {
                        flashcard.setPhoneticAudio(phonetic.getAudio());
                    }
                }
            }
        } catch (Exception e) {
            // Log error but continue with null phonetics
            System.out.println("Error getting phonetics: " + e.getMessage());
        }

        // Lưu flashcard vào cơ sở dữ liệu
        flashcardRepository.save(flashcard);

        // Tạo và trả về ResFlashcardDTO
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
        res.setExampleMeaning(selectedMeaning.getExampleMeaning());

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
        res.setExampleMeaning(flashcard.getExampleMeaning()); // Add this line

        return res;
    }

    public Page<Flashcard> getAllFlashcardsSortedByLatest(Long groupId, Pageable pageable) {
        if (groupId == null) {
            return flashcardRepository.findAllByOrderByLastReviewedDesc(pageable);
        }

        FlashcardGroup group = flashcardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard group not found"));
        return flashcardRepository.findByFlashcardGroupOrderByLastReviewedDesc(group, pageable);
    }

    public Page<Flashcard> getAllFlashcardsSortedByOldest(Long groupId, Pageable pageable) {
        if (groupId == null) {
            return flashcardRepository.findAllByOrderByLastReviewedAsc(pageable);
        }

        FlashcardGroup group = flashcardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard group not found"));
        return flashcardRepository.findByFlashcardGroupOrderByLastReviewedAsc(group, pageable);
    }

    public void markFlashcardAsUnlearned(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
        flashcard.setLearnedStatus(false); // Đánh dấu là chưa học
        flashcardRepository.save(flashcard);
    }
}