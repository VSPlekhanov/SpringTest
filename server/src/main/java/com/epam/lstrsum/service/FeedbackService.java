package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.feedback.FeedbackAllFieldsDto;
import com.epam.lstrsum.dto.feedback.FeedbackPostDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewFeedbackNotificationTemplate;
import org.springframework.web.multipart.MultipartFile;


public interface FeedbackService {

    @EmailNotification(template = NewFeedbackNotificationTemplate.class)
    FeedbackAllFieldsDto sendFeedback(FeedbackPostDto dtoObject, MultipartFile[] files);
}
