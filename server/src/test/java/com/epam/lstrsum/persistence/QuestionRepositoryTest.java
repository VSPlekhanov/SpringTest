package com.epam.lstrsum.persistence;

import com.epam.lstrsum.SetUpDataBaseCollections;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.TextCriteria;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

public class QuestionRepositoryTest extends SetUpDataBaseCollections {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findAllBy() throws Exception {
        final TextCriteria textCriteria =
                new TextCriteria().matchingAny("JsonMappingException", "I", "have", "this", "call", "in", "async", "task");

        final PageRequest pageRequest = new PageRequest(0, 100);

        assertThat(questionRepository.findAllBy(textCriteria, pageRequest).stream()
                .anyMatch(question -> question.getQuestionId().equals("1u_1r"))).isTrue();
    }

    @Test
    public void findAllByAllowedSubsContainsOrderByCreatedAtDesc() throws Exception {
        val someUser = userRepository.findByEmailIgnoreCase(EXISTING_USER_EMAIL).get();

        assertThat(questionRepository.findAllByAllowedSubsContainsOrderByCreatedAtDesc(
                someUser, new PageRequest(0, 100))
        ).hasSize(6);

    }

    @Test
    public void findAllByOrderByCreatedAtDesc() throws Exception {
        assertThat(questionRepository.findAllByOrderByCreatedAtDesc(new PageRequest(0, 2)))
                .hasSize(2);
    }

    @Test
    public void findAllByOrderByCreatedAtDescWithoutPaging() throws Exception {
        assertThat(questionRepository.findAllByOrderByCreatedAtDesc())
                .hasSize(questionRepository.findAll().size());
    }
}