package com.epam.lstrsum.testutils.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.activation.DataSource;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class CompositeMimeMessage {
    private MimeMessage mimeMessage;
    private List<String> cc;
    private List<String> to;
    private String subject;
    private String text;

    private List<Attach> attaches;

    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class Attach {
        private DataSource dataSource;
        private String name;
        private String description;
    }
}
