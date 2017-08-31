package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.AttachmentRepository;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.InstantiateUtil;
import com.epam.lstrsum.testutils.model.CompositeMimeMessage;
import lombok.SneakyThrows;
import microsoft.exchange.webservices.data.core.ExchangeService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.MimeMessageCreatorUtil.createCompositeMimeMessage;
import static com.epam.lstrsum.testutils.MimeMessageCreatorUtil.createFromFile;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ActiveProfiles(value = {"unsecured", "email"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class IntegrationMailReceiverTest {
    @Autowired
    private MailReceiver mailReceiver;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ExchangeService exchangeService;

    @MockBean
    private MailService mailService;

    @MockBean
    private TelescopeService telescopeService;

    @After
    public void tearDown() {
        questionRepository.deleteAll();
        attachmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void prepareTelescopeServiceForCorrectAnswer(Address[] allRecipients) {
        List<TelescopeEmployeeEntityDto> toBeReturned = Arrays.stream(allRecipients)
                .filter(a -> a instanceof InternetAddress)
                .map(a -> (InternetAddress) a)
                .map(InternetAddress::getAddress)
                .map(String::toLowerCase)
                .map(InstantiateUtil::someTelescopeEmployeeEntityDtoWithEmail)
                .collect(Collectors.toList());

        doReturn(toBeReturned)
                .when(telescopeService)
                .getUsersInfoByEmails(anySetOf(String.class));
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
        addAllUsers(singletonList(simpleMimeMessage.getMimeMessage().getFrom()[0].toString()));
        prepareTelescopeServiceForCorrectAnswer(simpleMimeMessage.getMimeMessage().getAllRecipients());

        mailReceiver.receiveMessageAndHandleIt(simpleMimeMessage.getMimeMessage());

        allowEmails.addAll(simpleMimeMessage.getCc());
        allowEmails.addAll(simpleMimeMessage.getTo());
        allowEmails.add(simpleMimeMessage.getMimeMessage().getFrom()[0].toString());

        Set<String> allowEmailsInLowerCase = allowEmails.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        assertThat(userRepository.findAll())
                .allMatch(user -> allowEmailsInLowerCase.contains(user.getEmail()));
        assertThat(questionRepository.findAll()).hasSize(1);
    }

    @Test
    @SneakyThrows
    public void receivedMessageWithOneInlineImageAndOneAttach() {
        MimeMessage mimeMessage = createFromFile("src/test/resources/mail/rawMessageInput");
        addAllUsers(singletonList(mimeMessage.getFrom()[0].toString()));

        prepareTelescopeServiceForCorrectAnswer(mimeMessage.getAllRecipients());

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
        List<TelescopeEmployeeEntityDto> dtos = userEmails.stream()
                .map(String::toLowerCase)
                .map(InstantiateUtil::someTelescopeEmployeeEntityDtoWithEmail)
                .collect(Collectors.toList());

        doReturn(dtos).when(telescopeService)
                .getUsersInfoByEmails(anySetOf(String.class));

        userService.addIfNotExistAllWithRole(userEmails, singletonList(UserRoleType.ADMIN));
    }
}
