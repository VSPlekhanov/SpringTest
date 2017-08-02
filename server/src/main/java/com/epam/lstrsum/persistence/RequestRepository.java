package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends MongoRepository<Request, String> {
    List<Request> findAllBy(TextCriteria criteria, Pageable pageable);

    List<Request> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Request> findAllByOrderByCreatedAtDesc();

    @CountQuery("{$text: {$search: ?0}}")
    int getTextSearchResultsCount(String query);

    Optional<Request> findRequestByTitleAndTextAndAuthorId(String subject, String requestText, User authorId);
}
