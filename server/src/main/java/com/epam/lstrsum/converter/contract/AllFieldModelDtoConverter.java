package com.epam.lstrsum.converter.contract;

public interface AllFieldModelDtoConverter<T, U> {
    U modelToAllFieldsDto(T t);
}
