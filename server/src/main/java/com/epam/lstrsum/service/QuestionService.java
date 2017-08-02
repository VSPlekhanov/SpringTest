package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.QuestionDtoConverter;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.mail.EmailNotification;
import com.epam.lstrsum.mail.template.NewQuestionNotificationTemplate;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.persistence.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;


@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "question")
public class QuestionService {

    private final static int QUESTION_TITLE_LENGTH = 5;
    private final static int QUESTION_TEXT_LENGTH = 5;
    private final static int MIN_PAGE_SIZE = 0;

    @Setter
    private int searchDefaultPageSize;

    @Setter
    private int searchMaxPageSize;

    private final QuestionDtoConverter questionDtoConverter;
    private final QuestionRepository questionRepository;

    public List<QuestionAllFieldsDto> findAll() {
        List<Question> questionList = questionRepository.findAll();
        return mapList(questionList, questionDtoConverter::modelToAllFieldsDto);
    }

    /**
     * Performs fulltext search (by db text index).
     *
     * @param searchQuery Phrase to find. Searches by every word separately and by different word's forms.
     * @param page Page number to show, begins from 0.
     * @param size Size of a page.
     * @return List of questions.
     */
    public List<QuestionAllFieldsDto> search(String searchQuery, Integer page, Integer size) {
        if (isNull(size) || size <= 0) {
            size = searchDefaultPageSize;
        }
        if (size > searchMaxPageSize) {
            size = searchMaxPageSize;
        }
        if (isNull(page) || page < MIN_PAGE_SIZE) {
            page = MIN_PAGE_SIZE;
        }

        Sort sort = new Sort("score");
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchQuery);
        List<Question> questionList = questionRepository.findAllBy(criteria, new PageRequest(page, size, sort));

        return questionList
                .stream()
                .map(questionDtoConverter::modelToAllFieldsDto)
                .collect(Collectors.toList());
    }

    public Integer getTextSearchResultsCount(String query) {
        return questionRepository.getTextSearchResultsCount(query);
    }

    public List<QuestionBaseDto> findAllQuestionsBaseDto(int questionPage, int questionAmount) {
        Pageable pageable = new PageRequest(questionPage, questionAmount);
        List<Question> questionList = questionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapList(questionList, questionDtoConverter::modelToBaseDto);
    }

    @EmailNotification(template = NewQuestionNotificationTemplate.class)
    public QuestionAllFieldsDto addNewQuestion(QuestionPostDto questionPostDto, String email) {
        validateQuestionData(questionPostDto, email);
        Question newQuestion = questionDtoConverter.questionPostDtoAndAuthorEmailToQuestion(questionPostDto, email);
        Question saved = questionRepository.save(newQuestion);
        return questionDtoConverter.modelToAllFieldsDto(saved);
    }

    public QuestionAllFieldsDto getQuestionAllFieldDtoByQuestionId(String questionId) {
        Question question = questionRepository.findOne(questionId);
        return questionDtoConverter.modelToAllFieldsDto(question);
    }

    public QuestionAppearanceDto getQuestoinAppearanceDtoByQuestoinId(String questionId) {
        Question question = questionRepository.findOne(questionId);
        return questionDtoConverter.modelToQuestionAppearanceDto(question);
    }

    public Question getQuestionById(String questionId){
        return questionRepository.findOne(questionId);
    }

    public boolean contains(String objectsId) {
        return questionRepository.findOne(objectsId) != null;
    }

    private void validateQuestionData(QuestionPostDto questionPostDto, String email) {
        if (questionPostDto == null) {
            throw new QuestionValidationException("Post question should have json for QuestionPostDto");
        }
        if (email == null) {
            throw new QuestionValidationException("probably should`nt appear at all, problems with SSO");
        }
        if ((questionPostDto.getText() == null) || (questionPostDto.getTitle() == null)) {
            throw new QuestionValidationException("null fields found in question " + questionPostDto.toJson());
        }
        if (questionPostDto.getTitle().length() < QUESTION_TITLE_LENGTH) {
            throw new QuestionValidationException("Title is too short " + questionPostDto.toJson());
        }
        if (questionPostDto.getText().length() < QUESTION_TEXT_LENGTH) {
            throw new QuestionValidationException("Text is too short " + questionPostDto.toJson());
        }
    }

    private static<T1,T2> List<T2> mapList(List<T1> list, Function<T1, T2> mapper) {
        List<T2> result = new ArrayList<>();

        for (T1 value : list) {
            result.add(mapper.apply(value));
        }

        return result;
    }
}
