package com.epam.lstrsum.service.impl.mock;

import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.ElasticSearchService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.epam.lstrsum.utils.FunctionalUtil.getList;
import static com.epam.lstrsum.utils.FunctionalUtil.getRandomString;

@Service
@Profile("standalone")
@Primary
@RequiredArgsConstructor
public class ElasticSearchServiceMockImpl implements ElasticSearchService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final CounterService counterService;

    @Override
    public String smartSearch(String searchQuery, int page, int size) {
        counterService.increment("elastic.smart.search");

        val response = getList(() -> Question.builder()
                .title(getRandomString())
                .text(getRandomString())
                .build());

        return toJson(response);
    }

    // todo implement normal behavior
    @Override
    public String advancedSearch(String searchQuery, List<String> metaTags, Integer page, Integer size) {
        return null;
    }

    private String toJson(List<Question> response) {
        try {
            return OBJECT_MAPPER.writeValueAsString(response);
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public String smartSearchWithAllowedSub(String searchQuery, int page, int size, String email) {
        return smartSearch(searchQuery, page, size);
    }
}
