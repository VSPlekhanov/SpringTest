package com.epam.lstrsum.converter;

import org.springframework.stereotype.Service;

@Service
public interface BasicModelDtoConverter<T, R> {
    R modelToBaseDto(T t);
}
