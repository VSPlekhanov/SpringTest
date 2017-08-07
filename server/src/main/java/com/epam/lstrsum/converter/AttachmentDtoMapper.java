package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttachmentDtoMapper {
    AttachmentAllFieldsDto modelToAllFieldsDto(Attachment attachment);
}
