package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewQuestionNotificationTemplate;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface QuestionService extends ElasticSearchService {
    List<QuestionAllFieldsDto> findAll();

    /**
     * Performs fulltext search (by db text index).
     *
     * @param searchQuery Phrase to find. Searches by every word separately and by different word's forms.
     * @param page        Page number to show, begins from 0.
     * @param size        Size of a page.
     * @return List of questions.
     */
    List<QuestionAllFieldsDto> search(String searchQuery, Integer page, Integer size);

    Long getTextSearchResultsCount(String query);

    List<QuestionWithAnswersCountDto> findAllQuestionsBaseDto(int questionPage, int questionAmount);

    List<QuestionWithAnswersCountDto> findAllQuestionBaseDtoWithAllowedSub(int questionPage, int questionAmount, String userEmail);

    List<String> getRelevantTags(String key);

    @EmailNotification(template = NewQuestionNotificationTemplate.class)
    Question addNewQuestion(QuestionPostDto questionPostDto, String email);

    Question addNewQuestion(QuestionPostDto questionPostDto, String email, MultipartFile[] files);

    QuestionAllFieldsDto getQuestionAllFieldDtoByQuestionId(String questionId);

    void delete(String id);

    void deleteSubscriptionsByQuestionId(String questionId);

    Optional<QuestionAppearanceDto> getQuestionAppearanceDtoByQuestionId(String questionId);

    Question getQuestionById(String questionId);

//    Question getQuestionByAnswerId(String answerId);

    Question findQuestionByTitleAndAuthorEmail(String title, User authorId);

    boolean contains(String objectsId);

    Long getQuestionCount();

    void addAttachmentsToQuestion(String questionId, List<String> attachmentIds);

    Long getQuestionCountWithAllowedSub(String userEmail);
}
