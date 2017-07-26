package com.epam.lstrsum.converter;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.persistence.AnswerRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

public class AnswerDtoConverterTest extends SetUpDataBaseCollections {
    @Autowired
    private AnswerDtoConverter answerConverter;

    @Autowired
    private UserDtoConverter userConverter;

    @Autowired
    private AnswerRepository answerRepository;

    @Test
    public void convertFromPostDtoToAnswerReturnsExpectedValue() {
        AnswerPostDto answerPostDto = new AnswerPostDto("1u_1r", "text");
        String authorEmail = "John_Doe@epam.com";
        Answer convertedAnswer = answerConverter.answerPostDtoAndAuthorEmailToAnswer(answerPostDto, authorEmail);
        assertThat(answerPostDto.getText(), is(equalTo(convertedAnswer.getText())));
    }

    @Test
    public void convertAnswerToDtoReturnsExpectedValue() {
        Answer answer = answerRepository.findOne("1u_1r_2a");
        AnswerAllFieldsDto allFieldsDto = answerConverter.modelToAllFieldsDto(answer);
        assertThat(answer.getText(), is(equalTo(allFieldsDto.getText())));
        assertThat(answer.getCreatedAt(), is(equalTo(allFieldsDto.getCreatedAt())));
        assertThat(userConverter.modelToBaseDto(answer.getAuthorId()), is(equalTo(allFieldsDto.getAuthorId())));
        assertThat(answer.getUpVote(), is(equalTo(allFieldsDto.getUpVote())));
    }
}
