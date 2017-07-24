package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import org.springframework.stereotype.Service;

@Service
public class AttachmentDtoConverter implements AllFieldModelDtoConverter<Attachment, AttachmentAllFieldsDto> {

    @Override
    public AttachmentAllFieldsDto modelToAllFieldsDto(Attachment attachment) {
        return new AttachmentAllFieldsDto(attachment.getId(), attachment.getName(), attachment.getType(), attachment.getData());
    }
}
