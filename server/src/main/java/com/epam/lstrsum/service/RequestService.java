
package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.RequestDtoConverter;
import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestAppearanceDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.exception.NoSuchRequestException;
import com.epam.lstrsum.exception.RequestValidationException;
import com.epam.lstrsum.mail.EmailNotification;
import com.epam.lstrsum.mail.template.NewRequestNotificationTemplate;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;


@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "request")
public class RequestService {

    private final static int REQUEST_TITLE_LENGTH = 5;
    private final static int REQUEST_TEXT_LENGTH = 5;
    private final static int MIN_PAGE_SIZE = 0;

    @Setter
    private int searchDefaultPageSize;

    @Setter
    private int searchMaxPageSize;

    private final RequestDtoConverter requestDtoConverter;
    private final RequestRepository requestRepository;

    public List<RequestAllFieldsDto> findAll() {
        List<Request> requestList = requestRepository.findAll();
        return mapList(requestList, requestDtoConverter::modelToAllFieldsDto);
    }

    /**
     * Performs fulltext search (by db text index).
     *
     * @param searchQuery Phrase to find. Searches by every word separately and by different word's forms.
     * @param page Page number to show, begins from 0.
     * @param size Size of a page.
     * @return List of requests.
     */
    public List<RequestAllFieldsDto> search(String searchQuery, Integer page, Integer size) {
        if (isNull(size) || size <= 0) {
            size = searchDefaultPageSize;
        }
        if (size > searchMaxPageSize) {
            size = searchMaxPageSize;
        }
        if (isNull(page) || page < MIN_PAGE_SIZE) {
            page = MIN_PAGE_SIZE;
        }

        Sort sort = new Sort("score");
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchQuery);
        List<Request> requestList = requestRepository.findAllBy(criteria, new PageRequest(page, size, sort));

        return requestList
                .stream()
                .map(requestDtoConverter::modelToAllFieldsDto)
                .collect(Collectors.toList());
    }

    public Integer getTextSearchResultsCount(String query) {
        return requestRepository.getTextSearchResultsCount(query);
    }

    public List<RequestBaseDto> findAllRequestsBaseDto(int requestPage, int requestAmount) {
        Pageable pageable = new PageRequest(requestPage, requestAmount);
        List<Request> requestList = requestRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapList(requestList, requestDtoConverter::modelToBaseDto);
    }

    @EmailNotification(template = NewRequestNotificationTemplate.class)
    public RequestAllFieldsDto addNewRequest(RequestPostDto requestPostDto, String email) {
        validateRequestData(requestPostDto, email);
        Request newRequest = requestDtoConverter.requestPostDtoAndAuthorEmailToRequest(requestPostDto, email);
        Request saved = requestRepository.save(newRequest);
        return requestDtoConverter.modelToAllFieldsDto(saved);
    }

    public RequestAllFieldsDto getRequestAllFieldDtoByRequestId(String requestId) {
        Request request = requestRepository.findOne(requestId);
        return requestDtoConverter.modelToAllFieldsDto(request);
    }

    public RequestAppearanceDto getRequestAppearanceDtoByRequestId(String requestId) {
        Request request = requestRepository.findOne(requestId);
        return requestDtoConverter.modelToRequestAppearanceDto(request);
    }

    public Request getRequestById(String requestId){
        return requestRepository.findOne(requestId);
    }

    public boolean contains(String objectsId) {
        return requestRepository.findOne(objectsId) != null;
    }

    private void validateRequestData(RequestPostDto requestPostDto, String email) {
        if (requestPostDto == null) {
            throw new RequestValidationException("Post request should have json for RequestPostDto");
        }
        if (email == null) {
            throw new RequestValidationException("probably should`nt appear at all, problems with SSO");
        }
        if ((requestPostDto.getText() == null) || (requestPostDto.getTitle() == null)) {
            throw new RequestValidationException("null fields found in request " + requestPostDto.toJson());
        }
        if (requestPostDto.getTitle().length() < REQUEST_TITLE_LENGTH) {
            throw new RequestValidationException("Title is too short " + requestPostDto.toJson());
        }
        if (requestPostDto.getText().length() < REQUEST_TEXT_LENGTH) {
            throw new RequestValidationException("Text is too short " + requestPostDto.toJson());
        }
    }

    private static<T1,T2> List<T2> mapList(List<T1> list, Function<T1, T2> mapper) {
        List<T2> result = new ArrayList<>();

        for (T1 value : list) {
            result.add(mapper.apply(value));
        }

        return result;
    }

    public Request findRequestByTitleAndTextAndAuthorId(String requestTitle, String requestText, User authorId) {
        return requestRepository.findRequestByTitleAndTextAndAuthorId(requestTitle, requestText, authorId).
                orElseThrow(() -> new NoSuchRequestException("No such a Request in request Collection"));
    }
}