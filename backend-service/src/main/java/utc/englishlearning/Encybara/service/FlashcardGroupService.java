package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import utc.englishlearning.Encybara.domain.Flashcard;
import utc.englishlearning.Encybara.domain.FlashcardGroup;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.FlashcardGroupRepository;
import utc.englishlearning.Encybara.repository.FlashcardRepository;
import utc.englishlearning.Encybara.repository.UserRepository;
import utc.englishlearning.Encybara.exception.ResourceAlreadyExistsException;
import utc.englishlearning.Encybara.exception.InvalidOperationException;

@Service
public class FlashcardGroupService {

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardGroupRepository flashcardGroupRepository;

    @Autowired
    private UserRepository userRepository;

    public FlashcardGroup createFlashcardGroup(String name, Long userId) {
        if (flashcardGroupRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Group with this name already exists");
        }

        FlashcardGroup group = new FlashcardGroup();
        group.setName(name);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        group.setUser(user);

        return flashcardGroupRepository.save(group);
    }

    public void addFlashcardToGroup(Long flashcardId, Long groupId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        FlashcardGroup group = flashcardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard group not found"));

        if (group.getName().equals("New Flashcards")) {
            throw new InvalidOperationException("Cannot add flashcard to the 'New Flashcards' group");
        }

        if (flashcard.getFlashcardGroup() != null
                && flashcard.getFlashcardGroup().getId() == groupId) {
            throw new InvalidOperationException("Flashcard is already in this group");
        }

        flashcard.setFlashcardGroup(group);
        flashcardRepository.save(flashcard);
    }

    public void removeFlashcardFromGroup(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        if (flashcard.getFlashcardGroup() != null && flashcard.getFlashcardGroup().getName().equals("New Flashcards")) {
            throw new InvalidOperationException("Cannot remove flashcard from the 'New Flashcards' group");
        }

        FlashcardGroup newFlashcardsGroup = flashcardGroupRepository.findByName("New Flashcards");
        if (newFlashcardsGroup == null) {
            newFlashcardsGroup = new FlashcardGroup();
            newFlashcardsGroup.setName("New Flashcards");
            newFlashcardsGroup = flashcardGroupRepository.save(newFlashcardsGroup);
        }

        flashcard.setFlashcardGroup(newFlashcardsGroup);
        flashcardRepository.save(flashcard);
    }

    public void deleteFlashcardGroup(Long groupId) {
        FlashcardGroup group = flashcardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard group not found"));

        if (group.getName().equals("New Flashcards")) {
            throw new InvalidOperationException("Cannot delete the 'New Flashcards' group");
        }

        flashcardGroupRepository.delete(group);
    }

    public FlashcardGroup updateFlashcardGroup(Long groupId, String newName) {
        FlashcardGroup group = flashcardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard group not found"));

        if (flashcardGroupRepository.existsByName(newName)) {
            throw new RuntimeException("Group with this name already exists");
        }

        group.setName(newName);
        return flashcardGroupRepository.save(group);
    }

    public Page<Flashcard> getFlashcardsInGroup(Long groupId, Pageable pageable, String word, Boolean learnedStatus,
            String vietnameseMeaning) {
        FlashcardGroup group = flashcardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard group not found"));

        return flashcardRepository.findAllByFlashcardGroupAndFilters(group, word, learnedStatus, vietnameseMeaning,
                pageable);
    }

    public Page<FlashcardGroup> getAllFlashcardGroupsByUserId(Long userId, Pageable pageable) {
        return flashcardGroupRepository.findAllByUserId(userId, pageable);
    }

}
