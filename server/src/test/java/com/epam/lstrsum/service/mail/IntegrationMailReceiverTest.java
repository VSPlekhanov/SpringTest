package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.testutils.model.CompositeMimeMessage;
import microsoft.exchange.webservices.data.core.ExchangeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.MimeMessageCreatorUtil.createSimpleMimeMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

@ActiveProfiles(value = {"unsecured", "email"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class IntegrationMailReceiverTest {

    @Autowired
    private MailReceiver mailReceiver;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ExchangeService exchangeService;

    @MockBean
    private MailService mailService;

    @Test
    public void correctParsingMimeMessage() throws Exception {
        final Set<String> allowEmails = userRepository.findAll().stream().map(User::getEmail).collect(Collectors.toSet());

        final CompositeMimeMessage simpleMimeMessage = createSimpleMimeMessage();
        for (final String email : simpleMimeMessage.getCc()) {
            doThrow(Exception.class).when(exchangeService).expandGroup(email);
        }
        for (final String email : simpleMimeMessage.getTo()) {
            doThrow(Exception.class).when(exchangeService).expandGroup(email);
        }

        addAllUsers(simpleMimeMessage.getTo());
        addAllUsers(Collections.singletonList(simpleMimeMessage.getMimeMessage().getFrom()[0].toString()));

        mailReceiver.showMessages(simpleMimeMessage.getMimeMessage());

        allowEmails.addAll(simpleMimeMessage.getCc());
        allowEmails.addAll(simpleMimeMessage.getTo());
        allowEmails.add(simpleMimeMessage.getMimeMessage().getFrom()[0].toString());

        assertThat(userRepository.findAll())
                .allMatch(user -> allowEmails.contains(user.getEmail()));
    }

    private void addAllUsers(List<String> userEmails) {
        User.UserBuilder builder = User.builder()
                .createdAt(Instant.now())
                .firstName("someName")
                .lastName("someLastName")
                .isActive(true)
                .roles(Collections.singletonList(UserRoleType.EXTENDED_USER));

        for (String userEmail : userEmails) {
            userRepository.save(builder.email(userEmail).build());
        }
    }
}
