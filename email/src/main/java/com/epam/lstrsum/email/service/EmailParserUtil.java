package com.epam.lstrsum.email.service;

import org.apache.commons.mail.util.MimeMessageParser;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class EmailParserUtil {
    public static String getReplyTo(MimeMessage mimeMessage) throws MessagingException {
        String[] replyTo = mimeMessage.getHeader("Reply-To");
        if (isNull(replyTo) || replyTo.length == 0) {
            return null;
        }
        return mimeMessage.getHeader("Reply-To")[0];
    }

    public static String getSender(MimeMessage mimeMessage) throws MessagingException {
        return ((InternetAddress) mimeMessage.getFrom()[0]).getAddress();
    }

    public static boolean stringIsEmpty(String questionText) {
        return questionText.trim().isEmpty();
    }

    public static String replaceStringKeys(String questionText, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            questionText = questionText.replace(keys.get(i), "mail.message.index:" + i);
        }

        return questionText;
    }

    public static Collection<String> evalKeysForInlining(String questionText) {
        Elements src = Jsoup.parse(questionText).select("img");

        return src.stream()
                .map(img -> img.attr("src"))
                .filter(el -> el.startsWith("cid:"))
                .collect(Collectors.toList());
    }

    public static Collection<DataSource> evalInlineSources(
            MimeMessageParser messageParser, String questionText
    ) {
        Elements src = Jsoup.parse(questionText).select("img");

        return src.stream()
                .map(img -> img.attr("src"))
                .filter(el -> el.startsWith("cid:"))
                .map(s -> s.substring(4))
                .map(messageParser::findAttachmentByCid)
                .collect(Collectors.toList());
    }
}
