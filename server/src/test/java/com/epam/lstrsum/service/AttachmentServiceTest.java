package com.epam.lstrsum.service;

import com.epam.lstrsum.aggregators.AttachmentAggregator;
import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.persistence.AttachmentRepository;
import com.epam.lstrsum.service.impl.AttachmentServiceImpl;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static com.epam.lstrsum.testutils.InstantiateUtil.someAttachmentAllFieldsDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AttachmentServiceTest {
    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private AttachmentAggregator aggregator;
    private AttachmentService attachmentService;


    @Before
    public void setUp() {
        initMocks(this);
        attachmentService = new AttachmentServiceImpl(attachmentRepository, aggregator);
    }

    @Test
    public void newAttachmentShouldBeSaved() throws Exception {
        val newAttachment = new AttachmentAllFieldsDto(null, "testFile", "image/jpeg", new byte[]{1, 2, 3});
        val attachment = new Attachment("id", newAttachment.getName(), newAttachment.getType(), newAttachment.getData());
        val expected = someAttachmentAllFieldsDto();

        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
        when(aggregator.modelToAllFieldsDto(any(Attachment.class))).thenReturn(expected);

        assertThat(attachmentService.save(newAttachment))
                .isEqualToComparingFieldByFieldRecursively(expected);

        verify(attachmentRepository, times(1)).save(any(Attachment.class));
    }

    @Test
    public void newMultipartFileShouldBeSaved() throws Exception {
        val originalFileName = "originalFileName";
        val contentType = "contentType";
        byte[] content = {1, 2, 3};

        MockMultipartFile file = new MockMultipartFile("TEMP_FILE_NAME", originalFileName, contentType, content);
        Attachment expected = Attachment.builder().id("id").name(originalFileName).type(contentType).data(content).build();

        when(attachmentRepository.save(any(Attachment.class))).thenReturn(expected);

        assertThat(attachmentService.saveMultipartFile(file)).isEqualTo("id");

        verify(attachmentRepository, times(1)).save(any(Attachment.class));
    }

    @Test
    public void findOneShouldFindExistingObject() throws Exception {
        val existingId = "someId";
        val att = new Attachment(existingId, "name", "type", new byte[]{1, 2, 3});
        when(attachmentRepository.findOne(existingId)).thenReturn(att);

        attachmentService.findOne(existingId);
        verify(attachmentRepository, times(1)).findOne(anyString());
        verify(aggregator, times(1)).modelToAllFieldsDto(any());
    }

    @Test
    public void findOneAllowedSubWithNonExistingQuestion() throws Exception {
        attachmentService.findOneAllowedSub(someString(), null, someString());
        verify(attachmentRepository, times(0)).findOne(anyString());
    }

    @Test
    public void fineOneShouldReturnEmptyOptionalIfNotFound() throws Exception {
        val notExistingId = "someId";
        when(attachmentRepository.findOne(notExistingId)).thenReturn(null);

        Optional<AttachmentAllFieldsDto> one = attachmentService.findOne(notExistingId);

        assertEquals(Optional.empty(), one);
    }

    @Test
    public void deleteShouldRemoveAttachment() throws Exception {
        val id = "id";

        attachmentService.delete(id);
        verify(attachmentRepository).delete(id);
    }
}
