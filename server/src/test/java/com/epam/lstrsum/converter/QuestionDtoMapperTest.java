package com.epam.lstrsum.converter;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestion;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUser;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUserBaseDto;
import static com.epam.lstrsum.utils.FunctionalUtil.getListWithSize;
import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDtoMapperTest extends SetUpDataBaseCollections {
    @Autowired
    private QuestionDtoMapper questionDtoMapper;

    public static void checkQuestionBaseDto(QuestionBaseDto questionBaseDto, Question question, UserBaseDto author) {
        assertThat(questionBaseDto.getQuestionId()).isEqualTo(question.getQuestionId());
        assertThat(questionBaseDto.getTitle()).isEqualTo(question.getTitle());
        assertThat(questionBaseDto.getAuthor()).isEqualToComparingFieldByField(author);
        assertThat(questionBaseDto.getCreatedAt()).isEqualTo(question.getCreatedAt());
        assertThat(questionBaseDto.getDeadLine()).isEqualTo(question.getDeadLine());
        assertThat(questionBaseDto.getTags()).isEqualTo(question.getTags());
    }

    @Test
    public void modelToBaseDto() throws Exception {
        Question question = someQuestion();
        UserBaseDto author = someUserBaseDto();

        assertThat(questionDtoMapper.modelToBaseDto(question, author))
                .satisfies(questionBaseDto -> checkQuestionBaseDto(questionBaseDto, question, author));
    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        Question question = someQuestion();
        UserBaseDto author = someUserBaseDto();
        List<UserBaseDto> allowedSubs = getListWithSize(InstantiateUtil::someUserBaseDto, 2);
        assertThat(questionDtoMapper.modelToAllFieldsDto(question, author, allowedSubs))
                .satisfies(
                        questionAllFieldsDto -> {
                            checkQuestionBaseDto(questionAllFieldsDto, question, author);
                            assertThat(questionAllFieldsDto.getText()).isEqualTo(question.getText());
                            assertThat(questionAllFieldsDto.getAllowedSubs()).containsExactly(allowedSubs.get(0), allowedSubs.get(1));
                        }
                );
    }

    @Test
    public void modelToQuestionAppearanceDto() {
        Question question = someQuestion();
        UserBaseDto author = someUserBaseDto();
        List<Attachment> attachments = getListWithSize(InstantiateUtil::someAttachment, 2);

        assertThat(questionDtoMapper.modelToQuestionAppearanceDto(question, author, attachments))
                .satisfies(
                        questionAppearanceDto -> {
                            checkQuestionBaseDto(questionAppearanceDto, question, author);
                            assertThat(questionAppearanceDto.getText()).isEqualTo(question.getText());
                            assertThat(questionAppearanceDto.getAttachments().size()).isEqualTo(2);
                            assertThat(questionAppearanceDto.getAttachments().get(0))
                                    .isEqualToIgnoringGivenFields(attachments.get(0), "size");
                            assertThat(questionAppearanceDto.getAttachments().get(1))
                                    .isEqualToIgnoringGivenFields(attachments.get(1), "size");
                        }
                );
    }

    @Test
    public void questionPostDtoAndAuthorEmailToQuestion() {
        QuestionPostDto questionPostDto = someQuestionPostDto();
        List<User> allowedSubs = getListWithSize(InstantiateUtil::someUser, 2);
        User user = someUser();

        assertThat(questionDtoMapper.questionPostDtoAndAuthorEmailToQuestion(questionPostDto, user, allowedSubs))
                .satisfies(
                        question -> {
                            assertThat(question.getTitle()).isEqualTo(questionPostDto.getTitle());
                            assertThat(question.getText()).isEqualTo(questionPostDto.getText());
                            assertThat(question.getTags()).isEqualTo(questionPostDto.getTags());
                            assertThat(question.getDeadLine()).isEqualTo(Instant.ofEpochMilli(questionPostDto.getDeadLine()));
                            assertThat(question.getAllowedSubs()).containsExactly(allowedSubs.get(0), allowedSubs.get(1));
                            assertThat(question.getAuthorId()).isEqualTo(user);
                            assertThat(question.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
                        }
                );
    }

    @Test
    public void questionPostDtoAndAuthorEmailAndAttachmentsToQuestion() {
        QuestionPostDto questionPostDto = someQuestionPostDto();
        List<User> allowedSubs = getListWithSize(InstantiateUtil::someUser, 2);
        User user = someUser();
        List<String> attachmentIds = getListWithSize(InstantiateUtil::someString, 2);

        assertThat(questionDtoMapper.questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(questionPostDto, user, allowedSubs, attachmentIds))
                .satisfies(
                        question -> {
                            assertThat(question.getTitle()).isEqualTo(questionPostDto.getTitle());
                            assertThat(question.getText()).isEqualTo(questionPostDto.getText());
                            assertThat(question.getTags()).isEqualTo(questionPostDto.getTags());
                            assertThat(question.getDeadLine()).isEqualTo(Instant.ofEpochMilli(questionPostDto.getDeadLine()));
                            assertThat(question.getAllowedSubs()).containsExactly(allowedSubs.get(0), allowedSubs.get(1));
                            assertThat(question.getAuthorId()).isEqualTo(user);
                            assertThat(question.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
                            assertThat(question.getAttachmentIds()).containsExactly(attachmentIds.get(0), attachmentIds.get(1));
                        }
                );
    }

    @Test
    public void subscriptionsToListOfQuestionBaseDto() {
        final int size = 2;
        List<Question> subscriptions = getListWithSize(InstantiateUtil::someQuestion, size);
        List<UserBaseDto> authors = getListWithSize(InstantiateUtil::someUserBaseDto, size);

        assertThat(questionDtoMapper.subscriptionsToListOfQuestionBaseDto(subscriptions, authors))
                .hasSize(size)
                .allMatch(Objects::nonNull);
    }
}