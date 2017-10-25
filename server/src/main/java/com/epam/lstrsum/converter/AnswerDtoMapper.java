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
            @Mapping(target = "upVote", expression = "java(answer.getVotes().size())"),
            @Mapping(target = "authorId", source = "authorId")

    })
    AnswerAllFieldsDto modelToAllFieldsDto(Answer answer, UserBaseDto authorId, QuestionBaseDto questionId);

    @Mappings({
            @Mapping(target = "authorId", source = "authorId"),
            @Mapping(target = "upVote", expression = "java(answer.getVotes().size())"),
            @Mapping(target = "userVoted", expression = "java(userVoted)")
    })
    AnswerBaseDto modelToBaseDto(Answer answer, UserBaseDto authorId, Boolean userVoted);


    default List<AnswerBaseDto> answersToQuestionInAnswerBaseDto(List<Answer> answers, List<UserBaseDto> authors, String currentUserEmail) {
        return IntStream.range(0, answers.size())
                .mapToObj(i -> {
                    Answer answer = answers.get(i);
                    UserBaseDto author = authors.get(i);
                    Boolean userVoted = answer.getVotes().stream().anyMatch(vote -> vote.getAuthorEmail().equals(currentUserEmail));
                    return modelToBaseDto(answer, author, userVoted);
                })
                .collect(Collectors.toList());
    }

    @Mappings({
            @Mapping(target = "text", source = "answer.text"),
            @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())"),
            @Mapping(target = "questionId", source = "questionId"),
            @Mapping(target = "authorId", source = "authorId"),
            @Mapping(target = "answerId", ignore = true),
            @Mapping(target = "votes", expression = "java(java.util.Collections.emptyList())")
    })
    Answer answerPostDtoAndAuthorEmailToAnswer(AnswerPostDto answer, User authorId, Question questionId);
}
