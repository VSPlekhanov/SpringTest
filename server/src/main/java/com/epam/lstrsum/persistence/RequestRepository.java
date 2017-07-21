package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Request;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RequestRepository extends MongoRepository<Request, String> {
    List<Request> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Request> findAllByOrderByCreatedAtDesc();
}
