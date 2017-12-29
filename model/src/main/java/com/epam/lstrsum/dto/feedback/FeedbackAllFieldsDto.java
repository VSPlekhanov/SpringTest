package com.epam.lstrsum.dto.feedback;

import com.epam.lstrsum.model.Attachment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class FeedbackAllFieldsDto {

    private String title;
    private String text;
    private List<Attachment> attachments;
}
