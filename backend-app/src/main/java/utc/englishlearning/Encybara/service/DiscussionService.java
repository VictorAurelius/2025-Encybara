package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.Discussion;
import utc.englishlearning.Encybara.domain.request.discussion.ReqCreateDiscussionDTO;
import utc.englishlearning.Encybara.domain.response.discussion.ResDiscussionDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.exception.InvalidOperationException;
import utc.englishlearning.Encybara.repository.DiscussionRepository;
import utc.englishlearning.Encybara.repository.LessonRepository;
import utc.englishlearning.Encybara.repository.UserRepository;
import utc.englishlearning.Encybara.repository.LikeRepository;

import java.util.List;

@Service
public class DiscussionService {

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Transactional
    public ResDiscussionDTO createDiscussion(ReqCreateDiscussionDTO reqCreateDiscussionDTO) {
        Discussion discussion = new Discussion();
        discussion.setUser(userRepository.findById(reqCreateDiscussionDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        discussion.setLesson(lessonRepository.findById(reqCreateDiscussionDTO.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found")));
        discussion.setContent(reqCreateDiscussionDTO.getContent());
        discussion.setNumLike(0);

        if (reqCreateDiscussionDTO.getParentId() != null) {
            Discussion parentDiscussion = discussionRepository.findById(reqCreateDiscussionDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent discussion not found"));

            List<Discussion> replies = discussionRepository.findByParentDiscussionId(parentDiscussion.getId());
            if (!replies.isEmpty()) {
                throw new InvalidOperationException("Parent discussion already has replies.");
            }

            for (Discussion reply : replies) {
                List<Discussion> subReplies = discussionRepository.findByParentDiscussionId(reply.getId());
                if (!subReplies.isEmpty()) {
                    throw new InvalidOperationException("Replies cannot have their own replies.");
                }
            }

            discussion.setParentDiscussion(parentDiscussion);
        }

        discussion = discussionRepository.save(discussion);
        return convertToDTO(discussion);
    }

    public List<ResDiscussionDTO> getAllDiscussionsByLessonId(Long lessonId) {
        List<Discussion> discussions = discussionRepository.findByLessonId(lessonId);
        return discussions.stream().map(this::convertToDTO).toList();
    }

    public ResDiscussionDTO getDiscussionById(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found"));
        return convertToDTO(discussion);
    }

    public List<ResDiscussionDTO> getAllDiscussionsByUserId(Long userId) {
        List<Discussion> discussions = discussionRepository.findByUserId(userId);
        return discussions.stream().map(this::convertToDTO).toList();
    }

    @Transactional
    public void deleteDiscussion(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found"));

        // Xóa tất cả các replies
        List<Discussion> replies = discussionRepository.findByParentDiscussionId(discussionId);
        for (Discussion reply : replies) {
            // Xóa likes của replies
            likeRepository.deleteByUserIdAndDiscussionId(reply.getUser().getId(), reply.getId());
            discussionRepository.delete(reply);
        }

        // Xóa likes của discussion
        likeRepository.deleteByUserIdAndDiscussionId(discussion.getUser().getId(), discussionId);
        discussionRepository.delete(discussion);
    }

    public Page<ResDiscussionDTO> getAllDiscussionsByLessonId(Long lessonId, Pageable pageable) {
        // Lấy tất cả thảo luận cha cho bài học
        Page<Discussion> discussions = discussionRepository.findByLessonIdAndParentDiscussionIsNull(lessonId, pageable);
        return discussions.map(this::convertToDTO);
    }

    public Page<ResDiscussionDTO> getAllDiscussionsByUserId(Long userId, Pageable pageable) {
        // Lấy tất cả thảo luận cha của người dùng
        Page<Discussion> discussions = discussionRepository.findByUserIdAndParentDiscussionIsNull(userId, pageable);
        return discussions.map(this::convertToDTO);
    }

    private ResDiscussionDTO convertToDTO(Discussion discussion) {
        ResDiscussionDTO dto = new ResDiscussionDTO();
        dto.setId(discussion.getId());
        dto.setUserId(discussion.getUser().getId());
        dto.setLessonId(discussion.getLesson().getId());
        dto.setContent(discussion.getContent());
        dto.setNumLike(discussion.getNumLike());

        // Lấy danh sách thảo luận con
        List<Discussion> replies = discussionRepository.findByParentDiscussionId(discussion.getId());
        List<ResDiscussionDTO> replyDTOs = replies.stream().map(this::convertToDTO).toList();
        dto.setReplies(replyDTOs); // Cập nhật danh sách replies

        return dto;
    }
}