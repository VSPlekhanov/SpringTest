package com.epam.lstrsum.service;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface TagService {

    List<String> getFilteredTagsRating(String key);
}