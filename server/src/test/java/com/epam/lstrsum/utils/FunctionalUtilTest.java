package com.epam.lstrsum.utils;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class FunctionalUtilTest {

    @Test
    @Repeat(value = 100)
    public void generateListTest() {
        final List<Object> someList = FunctionalUtil.getList(Answer::new, 120);

        assertThat(someList)
                .matches(list -> list.size() <= 120)
                .allMatch(Answer.class::isInstance);
    }

    @Test
    @Repeat(value = 100)
    public void generateListWithoutMaxSizeTest() {
        final List<Object> someList = FunctionalUtil.getList(Question::new);

        assertThat(someList)
                .matches(list -> list.size() <= 10)
                .allMatch(Question.class::isInstance);
    }

    @Test
    @Repeat(value = 100)
    public void generateEmailPostfix() {
        final String emailPostfix = FunctionalUtil.getEmailPostfix();

        assertThat(emailPostfix)
                .startsWith("@")
                .contains(".");
    }

}