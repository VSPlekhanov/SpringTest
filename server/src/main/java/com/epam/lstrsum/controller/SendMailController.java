package com.epam.lstrsum.controller;

import com.epam.lstrsum.configuration.MailConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@RestController

@RequestMapping("/api/send_mail")
@Import(MailConfiguration.class)
@Profile("email")

public class SendMailController {
    @Autowired
    private JavaMailSenderImpl mailSender;

    @GetMapping("/{data}")
    public String sendEmail(@PathVariable("data") String data) throws MessagingException {
        sendMessage("Test mail", data, "kate.son2015@yandex.ru");
        return "Done!";
    }

    private void sendMessage(String subject, String text, String ... to) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mailMsg = new MimeMessageHelper(mimeMessage);
        mailMsg.setFrom("Auto_EPM-LSTR_Ask_Exp@epam.com");
        mailMsg.setTo(to);
        mailMsg.setSubject(subject);
        mailMsg.setText(text);
        mailSender.send(mimeMessage);
    }
}
