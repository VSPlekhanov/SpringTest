package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.feedback.FeedbackPostDto;
import com.epam.lstrsum.service.FeedbackService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static com.epam.lstrsum.testutils.InstantiateUtil.*;
import static org.mockito.Mockito.*;

public class FeedbackControllerTest {

    @Mock
    private FeedbackService feedbackService = mock(FeedbackService.class);

    private FeedbackController feedbackController = new FeedbackController(feedbackService);

    @Test
    public void sendFeedback() throws IOException {
        FeedbackPostDto dtoObject = someFeedbackPostDto();
        MockMultipartFile[] files = someMockMultipartFiles();

        doReturn(someFeedbackAllFieldsDto())
                .when(feedbackService).sendFeedback(dtoObject, files);

        ResponseEntity<String> responseEntity = feedbackController.sendFeedback(dtoObject, files);

        verify(feedbackService, times(1)).sendFeedback(dtoObject, files);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}