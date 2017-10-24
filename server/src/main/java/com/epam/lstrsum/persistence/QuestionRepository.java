package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findAllBy(TextCriteria criteria, Pageable pageable);

    List<Question> findAllByAllowedSubsContainsOrderByCreatedAtDesc(User sub, Pageable pageable);

    List<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Question> findAllByOrderByCreatedAtDesc();

    @CountQuery("{$text: {$search: ?0}}")
    long getTextSearchResultsCount(String query);

    Optional<Question> findQuestionByTitleAndAuthorId(String title, User authorId);

//    Optional<Question> findOneByAnswers_AnswerId(String answerId);

    long countAllByAllowedSubs(User user);
}
