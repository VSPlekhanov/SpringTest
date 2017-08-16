package com.epam.lstrsum.converter;


import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.question.*;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mapper(componentModel = "spring")
public interface QuestionDtoMapper {

    QuestionBaseDto modelToBaseDto(Question question, UserBaseDto author);

    @Mappings({
            @Mapping(target = "answersCount", expression = "java( answerBaseDtos.size())")
    })
    QuestionWithAnswersCountDto modelToAnswersCount(Question question, UserBaseDto author, List<AnswerBaseDto> answerBaseDtos);

    @Mappings({
            @Mapping(target = "allowedSubs", source = "allowedSubs")
    })
    QuestionAllFieldsDto modelToAllFieldsDto(Question question, UserBaseDto author, List<UserBaseDto> allowedSubs);

    @Mappings({
            @Mapping(target = "answers", source = "answers")
    })
    QuestionAppearanceDto modelToQuestionAppearanceDto(Question question, UserBaseDto author, List<AnswerBaseDto> answers);

    @Mappings({
            @Mapping(target = "allowedSubs", source = "allowedSubs"),
            @Mapping(target = "deadLine", expression = "java( java.time.Instant.ofEpochMilli(questionPostDto.getDeadLine()))"),
            @Mapping(target = "authorId", source = "authorId"),
            @Mapping(target = "upVote", constant = "0"),
            @Mapping(target = "score", constant = "0"),
            @Mapping(target = "questionId", ignore = true),
            @Mapping(target = "attachmentIds", ignore = true)
    })
    Question questionPostDtoAndAuthorEmailToQuestion(QuestionPostDto questionPostDto, User authorId, List<User> allowedSubs);

    default List<QuestionBaseDto> subscriptionsToListOfQuestionBaseDto(List<Question> subscriptions, List<UserBaseDto> author) {
        return IntStream.range(0, subscriptions.size())
                .mapToObj(i -> modelToBaseDto(subscriptions.get(i), author.get(i)))
                .collect(Collectors.toList());
    }

}
