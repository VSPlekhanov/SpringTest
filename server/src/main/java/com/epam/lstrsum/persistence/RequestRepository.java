package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Request;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RequestRepository extends MongoRepository<Request, String> {
    List<Request> findAllBy(TextCriteria criteria, Pageable pageable);

    List<Request> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Request> findAllByOrderByCreatedAtDesc();

    @CountQuery("{$text: {$search: ?0}}")
    int getTextSearchResultsCount(String query);
}
