package com.epam.lstrsum.converter;

public interface BasicModelDtoConverter<T, R> {
    R modelToBaseDto(T t);
}
