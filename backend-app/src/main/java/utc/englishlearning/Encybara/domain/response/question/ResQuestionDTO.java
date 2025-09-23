package utc.englishlearning.Encybara.domain.response.question;

import utc.englishlearning.Encybara.domain.Question_Choice;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResQuestionDTO {
    private long id;
    private String quesContent;
    private String keyword;
    private QuestionTypeEnum quesType;
    private SkillTypeEnum skillType;
    private int point;
    private List<Question_Choice> questionChoices;
}