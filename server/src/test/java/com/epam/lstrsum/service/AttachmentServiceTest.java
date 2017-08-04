package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.AttachmentDtoConverter;
import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.persistence.AttachmentRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    private AttachmentDtoConverter converter = new AttachmentDtoConverter();

    private AttachmentService attachmentService;

    @Before
    public void setUp() {
        initMocks(this);
        attachmentService = new AttachmentService(attachmentRepository, converter);
    }

    @Test
    public void newAttachmentShouldBeSaved() throws Exception {
        AttachmentAllFieldsDto newAttachment = new AttachmentAllFieldsDto(null, "testFile", "image/jpeg", new byte[]{1, 2, 3});
        Attachment attachment = new Attachment(null, newAttachment.getFileName(), newAttachment.getFileType(), newAttachment.getData());

        when(attachmentRepository.save(attachment)).thenReturn(attachment);
        attachmentService.save(newAttachment);

        verify(attachmentRepository).save(attachment);
    }

    @Test
    public void newMultipartFileShouldBeSaved() throws Exception {
        String originalFileName = "originalFileName";
        String contentType = "contentType";
        byte[] content = {1, 2, 3};

        MockMultipartFile file = new MockMultipartFile("TEMP_FILE_NAME", originalFileName, contentType, content);
        Attachment expected = Attachment.builder().name(originalFileName).type(contentType).data(content).build();

        when(attachmentRepository.save(expected)).thenReturn(expected);
        attachmentService.saveMultipartFile(file);
        verify(attachmentRepository).save(expected);
    }

    @Test
    public void findOneShouldFindExistingObject() throws Exception {
        String existingId = "someId";
        Attachment att = new Attachment(existingId, "name", "type", new byte[]{1, 2, 3});
        when(attachmentRepository.findOne(existingId)).thenReturn(att);

        Optional<AttachmentAllFieldsDto> one = attachmentService.findOne(existingId);

        AttachmentAllFieldsDto expected = converter.modelToAllFieldsDto(att);
        AttachmentAllFieldsDto actual = one.orElseThrow(() -> new AssertionError("Expected not found!"));

        assertEquals(expected, actual);
    }

    @Test
    public void fineOneShouldReturnEmptyOptionalIfNotFound() throws Exception {
        String notExistingId = "someId";
        when(attachmentRepository.findOne(notExistingId)).thenReturn(null);

        Optional<AttachmentAllFieldsDto> one = attachmentService.findOne(notExistingId);

        assertEquals(Optional.empty(), one);
    }

    @Test
    public void deleteShouldRemoveAttachment() throws Exception {
        String id = "id";

        attachmentService.delete(id);
        verify(attachmentRepository).delete(id);
    }

}