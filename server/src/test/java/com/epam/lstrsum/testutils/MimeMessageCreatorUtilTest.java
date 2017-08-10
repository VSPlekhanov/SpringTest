package com.epam.lstrsum.testutils;

import com.epam.lstrsum.testutils.model.CompositeMimeMessage;
import lombok.SneakyThrows;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.MimeMessageCreatorUtil.createSimpleMimeMessage;
import static org.assertj.core.api.Assertions.assertThat;

public class MimeMessageCreatorUtilTest {

    @Test
    @SneakyThrows
    public void testCorrectCreation() {
        CompositeMimeMessage simpleMimeMessage = createSimpleMimeMessage();
        MimeMessage mimeMessage = simpleMimeMessage.getMimeMessage();

        assertThat(
                Arrays.stream(mimeMessage.getRecipients(Message.RecipientType.CC))
                        .map(a -> (InternetAddress) a)
                        .map(InternetAddress::getAddress)
                        .collect(Collectors.toList())
        ).isEqualTo(simpleMimeMessage.getCc());

        assertThat(Arrays.stream(mimeMessage.getRecipients(Message.RecipientType.TO))
                .map(a -> (InternetAddress) a)
                .map(InternetAddress::getAddress)
                .collect(Collectors.toList()))
                .isEqualTo(simpleMimeMessage.getTo());

        assertThat(mimeMessage.getSubject())
                .isEqualTo(simpleMimeMessage.getSubject());
    }

}
