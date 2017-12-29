package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.dto.feedback.FeedbackAllFieldsDto;
import com.epam.lstrsum.dto.feedback.FeedbackPostDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewFeedbackNotificationTemplate;
import com.epam.lstrsum.exception.FeedbackValidationException;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.service.FeedbackService;
import com.epam.lstrsum.utils.MessagesHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private final static int FEEDBACK_TITLE_LENGTH = 5;
    private final static int FEEDBACK_TEXT_LENGTH = 5;

    private final MessagesHelper messagesHelper;

    @Override
    @EmailNotification(template = NewFeedbackNotificationTemplate.class)
    public FeedbackAllFieldsDto sendFeedback(FeedbackPostDto dtoObject, MultipartFile[] files) {
        validateFeedbackData(dtoObject);
        List<Attachment> attachments = Arrays.stream(files)
                                             .map(file -> {
                                                 try {
                                                     return Attachment.builder()
                                                                      .data(file.getBytes())
                                                                      .name(file.getOriginalFilename())
                                                                      .type(file.getContentType())
                                                                      .build();
                                                 } catch (IOException e) {
                                                     log.error(e.getMessage());
                                                     return null;
                                                 }
                                             })
                                             .filter(Objects::nonNull)
                                             .collect(Collectors.toList());
        return new FeedbackAllFieldsDto(dtoObject.getTitle(), dtoObject.getText(), attachments);
    }

    private void validateFeedbackData(FeedbackPostDto feedbackPostDto) {
        if (feedbackPostDto == null) {
            throw new FeedbackValidationException(messagesHelper.get("validation.service.no-json-for-feedbackpostdto"));
        }
        if ((feedbackPostDto.getText() == null) || (feedbackPostDto.getTitle() == null)) {
            throw new FeedbackValidationException(MessageFormat.format(messagesHelper.get("validation.service.null-fields-in-feedback"),
                    feedbackPostDto.toJson()));
        }
        if (feedbackPostDto.getTitle().length() < FEEDBACK_TITLE_LENGTH) {
            throw new FeedbackValidationException(MessageFormat.format(messagesHelper.get("validation.service.short-title"),
                    feedbackPostDto.toJson()));
        }
        if (feedbackPostDto.getText().length() < FEEDBACK_TEXT_LENGTH) {
            throw new FeedbackValidationException(MessageFormat.format(messagesHelper.get("validation.service.short-text"),
                    feedbackPostDto.toJson()));
        }
    }
}
