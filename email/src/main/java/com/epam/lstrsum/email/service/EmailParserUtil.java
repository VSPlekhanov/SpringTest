package com.epam.lstrsum.email.service;

import org.jsoup.Jsoup;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static java.util.Objects.isNull;

public class EmailParserUtil {
    public static String mapPlainAnswerString(String line) {
        if (lineIsReply(line)) {
            return "";
        } else {
            return line + "\n";
        }
    }

    private static boolean lineIsReply(String line) {
        return (line.startsWith("On") && line.endsWith("wrote:")) || (line.startsWith(">"));
    }

    public static String getRequestTextFromHtmlAnswer(String htmlContent) {
        return Jsoup.parse(htmlContent).select("div").last().text();
    }

    public static String getAnswerTextFromHtmlAnswer(String htmlText) {
        return Jsoup.parseBodyFragment(htmlText).select("div").first().text();
    }

    public static String getReplyTo(MimeMessage mimeMessage) throws MessagingException {
        String[] replyTo = mimeMessage.getHeader("Reply-To");
        if (isNull(replyTo) || replyTo.length == 0) {
            return null;
        }
        return mimeMessage.getHeader("Reply-To")[0];
    }

    public static String getSender(MimeMessage mimeMessage) throws MessagingException {
        return mimeMessage.getFrom()[0].toString();
    }
}
