package com.epam.lstrsum.testutils;

import com.epam.lstrsum.testutils.model.CompositeMimeMessage;
import org.apache.commons.mail.HtmlEmail;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MimeMessageCreatorUtil {
    private static Random RANDOM = new Random();
    private static final int MINIMUM_TEXT_SIZE = 200;
    private static final int MAXIMUM_TEXT_SIZE = 500;
    private static final int MINIMUM_TITLE_SIZE = 10;
    private static final int MAXIMUM_TITLE_SIZE = 100;
    private static final int MAXIMUM_CC_SIZE = 100;
    private static char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static int LENGTH = CHARSET_AZ_09.length;
    private static String FAKE_EMAIL_ADDRESS = "fake@email.com";
    private static String HOST_NAME = "smtp.office365.com";

    public static CompositeMimeMessage createSimpleMimeMessage() {
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

            email.buildMimeMessage();
            builder.mimeMessage(email.getMimeMessage());

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
