package com.epam.lstrsum.converter;

public interface AllFieldModelDtoConverter<T, U> {
    U modelToAllFieldsDto(T t);
}
