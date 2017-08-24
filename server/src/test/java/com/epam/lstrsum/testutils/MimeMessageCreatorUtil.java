package com.epam.lstrsum.testutils;

import com.epam.lstrsum.testutils.model.CompositeMimeMessage;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.util.MimeMessageUtils;

import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MimeMessageCreatorUtil {
    private static final int MINIMUM_TEXT_SIZE = 200;
    private static final int MAXIMUM_TEXT_SIZE = 500;
    private static final int MINIMUM_TITLE_SIZE = 10;
    private static final int MAXIMUM_TITLE_SIZE = 100;
    private static final int MAXIMUM_CC_SIZE = 100;
    private static Random RANDOM = new Random();
    private static char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static int LENGTH = CHARSET_AZ_09.length;
    private static String FAKE_EMAIL_ADDRESS = "fake@email.com";
    private static String HOST_NAME = "smtp.office365.com";

    public static MimeMessage createFromFile(String fileName) throws IOException, MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.example.com");
        properties.put("mail.smtp.port", "25");
        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage mimeMessage = MimeMessageUtils.createMimeMessage(session, new File(fileName));

        mimeMessage.setFrom(FAKE_EMAIL_ADDRESS);
        mimeMessage.setSubject(generateTitle());

        mimeMessage.addRecipients(Message.RecipientType.TO, generateEmail());

        return mimeMessage;
    }

    public static MimeMessage createSimpleMimeMessage() throws EmailException {
        HtmlEmail email = new HtmlEmail();

        email.setHostName(HOST_NAME);
        email.setFrom(FAKE_EMAIL_ADDRESS);
        addCc(email);
        addTo(email);

        String subject = generateTitle();
        email.setSubject(subject);

        String text = generateText();
        email.setMsg(text);

        email.buildMimeMessage();

        return email.getMimeMessage();
    }

    public static CompositeMimeMessage createCompositeMimeMessage() {
        try {
            CompositeMimeMessage.CompositeMimeMessageBuilder builder = CompositeMimeMessage.builder();
            HtmlEmail email = new HtmlEmail();

            email.setHostName(HOST_NAME);
            email.setFrom(FAKE_EMAIL_ADDRESS);
            addCc(email, builder);
            addTo(email, builder);


            String subject = generateTitle();
            email.setSubject(subject);
            builder.subject(subject);

            String text = generateText();
            email.setMsg(text);
            builder.text(text);

            CompositeMimeMessage.Attach allowedAttach = CompositeMimeMessage.Attach.builder()
                    .dataSource(new ByteArrayDataSource(new byte[]{0, 1, 2, 3}, "text/plain"))
                    .name(ByteArrayDataSource.class.getName() + ".jpg")
                    .description("It is a " + ByteArrayDataSource.class.getName())
                    .build();

            CompositeMimeMessage.Attach notAllowedAttach = CompositeMimeMessage.Attach.builder()
                    .dataSource(new ByteArrayDataSource(new byte[]{0, 1, 2, 3}, "text/plain"))
                    .name("file.exe")
                    .description("It is an exe file ")
                    .build();

            CompositeMimeMessage.Attach allowedButBigAttach = CompositeMimeMessage.Attach.builder()
                    .dataSource(new FileDataSource(new File("src/test/resources/data/bigfile.jpg")))
                    .name("bigfile.log")
                    .description("description")
                    .build();

            email.attach(allowedAttach.getDataSource(), allowedAttach.getName(), allowedAttach.getDescription());
            email.attach(notAllowedAttach.getDataSource(), notAllowedAttach.getName(), notAllowedAttach.getDescription());
            email.attach(allowedButBigAttach.getDataSource(), allowedButBigAttach.getName(), allowedButBigAttach.getDescription());

            email.buildMimeMessage();
            builder.mimeMessage(email.getMimeMessage());
            builder.attaches(Collections.singletonList(allowedAttach));

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addTo(HtmlEmail email) {
        List<String> collect = Stream.generate(MimeMessageCreatorUtil::generateEmail)
                .limit(RANDOM.nextInt(MAXIMUM_CC_SIZE))
                .collect(Collectors.toList());

        collect.forEach(to -> addAddress(email, to));
    }

    private static void addTo(HtmlEmail email, CompositeMimeMessage.CompositeMimeMessageBuilder builder) {
        List<String> collect = Stream.generate(MimeMessageCreatorUtil::generateEmail)
                .limit(RANDOM.nextInt(MAXIMUM_CC_SIZE))
                .collect(Collectors.toList());
        builder.to(collect);

        collect.forEach(to -> addAddress(email, to));
    }

    private static void addAddress(HtmlEmail email, String to) {
        try {
            email.addTo(to);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addCc(HtmlEmail email) {
        List<String> collect = Stream.generate(MimeMessageCreatorUtil::generateEmail)
                .limit(RANDOM.nextInt(MAXIMUM_CC_SIZE))
                .collect(Collectors.toList());

        collect.forEach(cc -> addCcThrowable(email, cc));
    }

    private static void addCc(HtmlEmail email, CompositeMimeMessage.CompositeMimeMessageBuilder builder) {
        List<String> collect = Stream.generate(MimeMessageCreatorUtil::generateEmail)
                .limit(RANDOM.nextInt(MAXIMUM_CC_SIZE))
                .collect(Collectors.toList());
        builder.cc(collect);

        collect.forEach(cc -> addCcThrowable(email, cc));
    }

    private static void addCcThrowable(HtmlEmail email, String cc) {
        try {
            email.addCc(cc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateText() {
        return generateString(MINIMUM_TEXT_SIZE, MAXIMUM_TEXT_SIZE);
    }

    private static String generateTitle() {
        return generateString(MINIMUM_TITLE_SIZE, MAXIMUM_TITLE_SIZE);
    }

    private static String generateEmail() {
        return generateString(20, 40) + "@" + generateString(10, 20) + "." + generateString(5, 10);
    }

    private static String generateString(int minimumSize, int maximumSize) {
        if (maximumSize <= minimumSize) {
            throw new IllegalArgumentException("maximumSize <= minimumSize");
        }

        int size = minimumSize + RANDOM.nextInt(maximumSize - minimumSize);
        StringBuilder result = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            result.append(CHARSET_AZ_09[RANDOM.nextInt(LENGTH)]);
        }

        return result.toString();
    }
}
