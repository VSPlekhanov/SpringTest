package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Request;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RequestRepository extends MongoRepository<Request, String> {
    @Query("{$text: {$search: ?0}}")
    List<Request> search(String s);

    List<Request> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Request> findAllByOrderByCreatedAtDesc();
}
