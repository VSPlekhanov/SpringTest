package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.dto.attachment.AttachmentPropertiesDto;
import com.epam.lstrsum.model.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttachmentDtoMapper {
    AttachmentAllFieldsDto modelToAllFieldsDto(Attachment attachment);

    @Mappings({
            @Mapping(target = "size", expression = "java( attachment.getData().length)")
    })
    AttachmentPropertiesDto modelToPropertiesDto(Attachment attachment);
}
