package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.AttachmentRepository;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.model.CompositeMimeMessage;
import lombok.SneakyThrows;
import microsoft.exchange.webservices.data.core.ExchangeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.MimeMessageCreatorUtil.createCompositeMimeMessage;
import static com.epam.lstrsum.testutils.MimeMessageCreatorUtil.createFromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ActiveProfiles(value = {"unsecured", "email"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class IntegrationMailReceiverTest {
    private static final String FAKE_EMAIL = "fake@email.com";

    @Autowired
    private MailReceiver mailReceiver;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Spy
    private UserService userService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ExchangeService exchangeService;

    @MockBean
    private MailService mailService;

    @Before
    public void setUp() {
        User newUser = User.builder().email(FAKE_EMAIL).build();
        userRepository.save(newUser);
        Question newQuestion = Question.builder()
                .authorId(newUser)
                .inlineSources(Collections.singletonList(new byte[]{0}))
                .text("src=\"mail.message.index:0\"")
                .build();
        questionRepository.save(newQuestion);
    }

    @After
    public void tearDown() {
        userRepository.deleteAll();
        questionRepository.deleteAll();
        attachmentRepository.deleteAll();
    }

    @Test
    public void correctParsingMimeMessage() throws Exception {
        final Set<String> allowEmails = userRepository.findAll().stream().map(User::getEmail).collect(Collectors.toSet());

        final CompositeMimeMessage simpleMimeMessage = createCompositeMimeMessage();
        for (final String email : simpleMimeMessage.getCc()) {
            doThrow(Exception.class).when(exchangeService).expandGroup(email);
        }
        for (final String email : simpleMimeMessage.getTo()) {
            doThrow(Exception.class).when(exchangeService).expandGroup(email);
        }

        addAllUsers(simpleMimeMessage.getTo());
        addAllUsers(Collections.singletonList(simpleMimeMessage.getMimeMessage().getFrom()[0].toString()));

        mailReceiver.receiveMessageAndHandleIt(simpleMimeMessage.getMimeMessage());

        allowEmails.addAll(simpleMimeMessage.getCc());
        allowEmails.addAll(simpleMimeMessage.getTo());
        allowEmails.add(simpleMimeMessage.getMimeMessage().getFrom()[0].toString());

        assertThat(userRepository.findAll())
                .allMatch(user -> allowEmails.contains(user.getEmail()));
        assertThat(questionRepository.findAll()).hasSize(1);
    }

    @Test
    @SneakyThrows
    public void receivedMessageWithOneInlineImageAndOneAttach() {
        MimeMessage mimeMessage = createFromFile("src/test/resources/mail/rawMessageInput");
        addAllUsers(Collections.singletonList(mimeMessage.getFrom()[0].toString()));

        mailReceiver.receiveMessageAndHandleIt(mimeMessage);

        assertThat(mimeMessage).isNotNull();
        List<Question> actual = questionRepository.findAll();
        assertThat(actual).hasSize(1);

        Question question = actual.get(0);

        assertThat(question).isNotNull()
                .satisfies(this::hasInlineSource)
                .satisfies(this::hasNotAttachments)
                .satisfies(this::hasSourceInText);

        assertThat(attachmentRepository.findAll()).hasSize(0);
    }

    private void hasNotAttachments(Question question) {
        assertThat(question.getAttachmentIds())
                .isNull();
    }

    private void hasSourceInText(Question question) {
        assertThat(question.getText())
                .contains("src=\"mail.message.index:0\"");
    }

    private void hasInlineSource(Question question) {
        assertThat(question.getInlineSources())
                .hasSize(1)
                .element(0)
                .isNotNull();
    }

    private void addAllUsers(List<String> userEmails) {
        doReturn(1L).when(userService).addIfNotExistAllWithRole(any(), any());

        for (String userEmail : userEmails) {
            userService.addIfNotExistAllWithRole(
                    Collections.singletonList(userEmail), Collections.singletonList(UserRoleType.ADMIN)
            );
        }
    }
}
