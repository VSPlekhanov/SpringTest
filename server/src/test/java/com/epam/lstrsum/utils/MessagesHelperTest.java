package com.epam.lstrsum.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.MessageFormat;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles({"unsecured", "test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class MessagesHelperTest {

    @Autowired
    private MessagesHelper messagesHelper;

    @Test
    public void findExistingMessage() {
        assertThat(messagesHelper.get("validation.security.user-not-found")). isEqualTo("User not found");
    }

    @Test
    public void findNotExistingMessage() {
        assertThat(messagesHelper.get("validation.security.user-not")). isEqualTo("validation.security.user-not");
    }

    @Test
    public void findExistingMessageWithParams() {
        String msg = MessageFormat.format(messagesHelper.get("validation.service.no-such-question-with-id"),
                "id");
        assertThat(msg). isEqualTo("No such question with id : id");
    }

    @Test
    public void findExistingMessageWithParamsAndQuotes() {
        String msg = MessageFormat.format(messagesHelper.get("validation.service.question-not-exist-or-user-has-no-permission-to-question"),
                "email@epam.com", "id");
        assertThat(msg). isEqualTo("Question isn't exist or user with email : \" email@epam.com \" has no permission to question id : \" id \" and relative answers!");
    }

}
