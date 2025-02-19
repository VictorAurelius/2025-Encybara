package utc.englishlearning.Encybara.specification;

import org.springframework.data.jpa.domain.Specification;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;

public class QuestionSpecification {
    public static Specification<Question> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("keyword"), "%" + keyword + "%");
        };
    }

    public static Specification<Question> hasQuesContent(String content) {
        return (root, query, criteriaBuilder) -> {
            if (content == null || content.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("quesContent"), "%" + content + "%");
        };
    }

    public static Specification<Question> hasQuesType(QuestionTypeEnum quesType) {
        return (root, query, criteriaBuilder) -> {
            if (quesType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("quesType"), quesType);
        };
    }

    public static Specification<Question> hasPoint(int point) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("point"), point);
        };
    }

    // Thêm các phương thức Specification khác nếu cần
}