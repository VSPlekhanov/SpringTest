package com.epam.lstrsum.testutils.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
}
