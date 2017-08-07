package com.epam.lstrsum.converter.contract;

import org.springframework.stereotype.Service;

@Service
public interface BasicModelDtoConverter<T, R> {
    R modelToBaseDto(T t);
}
