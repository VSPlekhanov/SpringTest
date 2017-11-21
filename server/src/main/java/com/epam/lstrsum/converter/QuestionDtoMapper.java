package com.epam.lstrsum.converter;


import com.epam.lstrsum.dto.attachment.AttachmentPropertiesDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import com.epam.lstrsum.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;

@Mapper(componentModel = "spring")
public interface QuestionDtoMapper {

    QuestionBaseDto modelToBaseDto(Question question, UserBaseDto author);

    @Mappings({
            @Mapping(target = "answersCount", source = "answersCount.count"),
            @Mapping(target = "questionId", source = "question.questionId")
    })
    QuestionWithAnswersCountDto modelToAnswersCount(Question question, UserBaseDto author, QuestionWithAnswersCount answersCount);

    @Mappings({
            @Mapping(target = "allowedSubs", source = "allowedSubs")
    })
    QuestionAllFieldsDto modelToAllFieldsDto(Question question, UserBaseDto author, List<UserBaseDto> allowedSubs);

    QuestionAppearanceDto modelToQuestionAppearanceDto(Question question, UserBaseDto author, List<Attachment> attachments);

    @Mappings({
            @Mapping(target = "allowedSubs", source = "allowedSubs"),
            @Mapping(target = "tags", expression = "java( emptyStringArrayIfNull(questionPostDto.getTags()) )"),
            @Mapping(target = "deadLine", expression = "java( java.time.Instant.ofEpochMilli(questionPostDto.getDeadLine()) )"),
            @Mapping(target = "createdAt", expression = "java( java.time.Instant.now() )"),
            @Mapping(target = "authorId", source = "author"),
            @Mapping(target = "score", constant = "0"),
            @Mapping(target = "questionId", ignore = true),
            @Mapping(target = "attachmentIds", ignore = true),
            @Mapping(target = "answers", ignore = true)
    })
    Question questionPostDtoAndAuthorEmailToQuestion(QuestionPostDto questionPostDto, User author, List<User> allowedSubs);

    @Mappings({
            @Mapping(target = "allowedSubs", source = "allowedSubs"),
            @Mapping(target = "tags", expression = "java( emptyStringArrayIfNull(questionPostDto.getTags()) )"),
            @Mapping(target = "deadLine", expression = "java( java.time.Instant.ofEpochMilli(questionPostDto.getDeadLine()) )"),
            @Mapping(target = "createdAt", expression = "java( java.time.Instant.now() )"),
            @Mapping(target = "authorId", source = "author"),
            @Mapping(target = "score", constant = "0"),
            @Mapping(target = "questionId", ignore = true),
            @Mapping(target = "attachmentIds", source = "attachmentIds"),
            @Mapping(target = "answers", ignore = true)
    })
    Question questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(QuestionPostDto questionPostDto, User author, List<User> allowedSubs,
            List<String> attachmentIds);

    default List<QuestionBaseDto> subscriptionsToListOfQuestionBaseDto(List<Question> subscriptions, List<UserBaseDto> authors) {
        return IntStream.range(0, subscriptions.size())
                .mapToObj(i -> modelToBaseDto(subscriptions.get(i), authors.get(i)))
                .collect(Collectors.toList());
    }

    default String[] emptyStringArrayIfNull(String[] arrayToCheck) {
        return isNull(arrayToCheck) ? new String[]{} : arrayToCheck;
    }

    @Mappings({
            @Mapping(target = "size", expression = "java( attachment.getData().length)")
    })
    AttachmentPropertiesDto attachmentToAttachmentPropertiesDto(Attachment attachment);
}
