package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mapper(componentModel = "spring")
public interface AnswerDtoMapper {
    @Mappings({
            @Mapping(target = "questionId", source = "questionId"),
            @Mapping(target = "createdAt", source = "answer.createdAt"),
            @Mapping(target = "upVote", source = "answer.upVote"),
            @Mapping(target = "authorId", source = "authorId")

    })
    AnswerAllFieldsDto modelToAllFieldsDto(Answer answer, UserBaseDto authorId, QuestionBaseDto questionId);

    @Mappings({
            @Mapping(target = "authorId", source = "authorId")
    })
    AnswerBaseDto modelToBaseDto(Answer answer, UserBaseDto authorId);


    default List<AnswerBaseDto> answersToQuestionInAnswerBaseDto(List<Answer> answers, List<UserBaseDto> authors) {
        return IntStream.range(0, answers.size())
                .mapToObj(i -> modelToBaseDto(answers.get(i), authors.get(i)))
                .collect(Collectors.toList());
    }

    @Mappings({
            @Mapping(target = "text", source = "answer.text"),
            @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())"),
            @Mapping(target = "upVote", constant = "0"),
            @Mapping(target = "questionId", source = "questionId"),
            @Mapping(target = "authorId", source = "authorId"),

    })
    Answer answerPostDtoAndAuthorEmailToAnswer(AnswerPostDto answer, User authorId, Question questionId);
}
