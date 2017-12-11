package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.QuestionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.converter.contract.AllFieldModelDtoConverter;
import com.epam.lstrsum.converter.contract.BasicModelDtoConverter;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class QuestionAggregator implements BasicModelDtoConverter<Question, QuestionBaseDto>,
        AllFieldModelDtoConverter<Question, QuestionAllFieldsDto> {

    private final UserDtoMapper userMapper;
    private final QuestionDtoMapper questionMapper;

    private final AttachmentRepository attachmentRepository;

    private final UserAggregator userAggregator;

    @Override
    public QuestionAllFieldsDto modelToAllFieldsDto(Question question) {
        return questionMapper.modelToAllFieldsDto(
                question,
                userMapper.modelToBaseDto(question.getAuthorId()),
                userMapper.usersToListOfBaseDtos(question.getAllowedSubs())
        );
    }

    @Override
    public QuestionBaseDto modelToBaseDto(Question question) {
        return questionMapper.modelToBaseDto(
                question,
                userMapper.modelToBaseDto(question.getAuthorId())
        );
    }

    public QuestionWithAnswersCountDto modelToAnswersCountDto(QuestionWithAnswersCount questionWithAnswersCount) {
        final Question source = questionWithAnswersCount.getQuestionId();

        return questionMapper.modelToAnswersCount(
                source,
                userMapper.modelToBaseDto(source.getAuthorId()),
                questionWithAnswersCount
        );
    }

    public QuestionAppearanceDto modelToQuestionAppearanceDto(Question question, String currentUserEmail) {
        List<String> attachmentIds = question.getAttachmentIds();
        List<Attachment> attachments = (nonNull(attachmentIds) && !attachmentIds.isEmpty()) ?
                (List<Attachment>) attachmentRepository.findAll(attachmentIds) :
                Collections.emptyList();

        return questionMapper.modelToQuestionAppearanceDto(
                question,
                userMapper.modelToBaseDto(question.getAuthorId()),
                attachments,
                isCurrentUserSubscribe(question, currentUserEmail)
        );
    }

    public Question questionPostDtoAndAuthorEmailToQuestion(QuestionPostDto questionPostDto, String email) {
        User author = userAggregator.findByEmail(email);
        List<User> allowedSubs = getEmptyListIfNull(questionPostDto.getAllowedSubs());
        return questionMapper.questionPostDtoAndAuthorEmailToQuestion(
                questionPostDto,
                author,
                allowedSubs,
                initializeSubscribersListByAllowedSubsList(allowedSubs)
        );
    }

    public Question questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(QuestionPostDto questionPostDto, String email,
            List<String> attachmentIds) {
        User author = userAggregator.findByEmail(email);
        List<User> allowedSubs = getEmptyListIfNull(questionPostDto.getAllowedSubs());

        return questionMapper.questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(
                questionPostDto,
                author,
                allowedSubs,
                attachmentIds,
                initializeSubscribersListByAllowedSubsList(allowedSubs)
        );
    }

    private boolean isCurrentUserSubscribe(Question question, String userEmail){
        return question.getSubscribers()
                .stream()
                .map(User::getEmail)
                .anyMatch(userEmail::equalsIgnoreCase);
    }

    private List<User> initializeSubscribersListByAllowedSubsList(List<User> allowedSubs) {
        return allowedSubs;
    }

    public List<QuestionBaseDto> subscriptionsToListOfQuestionBaseDto(List<Question> subscriptions) {
        return questionMapper.subscriptionsToListOfQuestionBaseDto(
                subscriptions,
                subscriptions.stream()
                        .map(Question::getAuthorId)
                        .map(userMapper::modelToBaseDto)
                        .collect(Collectors.toList())
        );
    }

    private List<User> getEmptyListIfNull(List<String> emails) {
        return isNull(emails) ? Collections.emptyList() :
                emails.stream()
                        .map(userAggregator::findByEmail)
                        .collect(Collectors.toList());
    }
}
