package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestAppearanceDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.service.RequestService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/request")
@ConfigurationProperties(prefix = "request")
public class RequestController {

    @Setter
    private int maxRequestAmount;

    private final RequestService requestService;

    private final UserRuntimeRequestComponent userRuntimeRequestComponent;

    @Autowired
    public RequestController(RequestService requestService, UserRuntimeRequestComponent userRuntimeRequestComponent) {
        this.requestService = requestService;
        this.userRuntimeRequestComponent = userRuntimeRequestComponent;
    }

    @PostMapping()
    public ResponseEntity<String> addRequest(@RequestBody() RequestPostDto dtoObject)
            throws IOException {
        String email = userRuntimeRequestComponent.getEmail();
        String requestId = requestService.addNewRequest(dtoObject, email).getRequestId();
        return ResponseEntity.ok(requestId);
    }

    @GetMapping(value = "/{requestId}")
    public ResponseEntity<RequestAppearanceDto> getRequestWithAnswers(@PathVariable String requestId) {
        if (requestService.contains(requestId)) {
            RequestAppearanceDto requestDto = requestService.getRequestAppearanceDtoByRequestId(requestId);
            return ResponseEntity.ok(requestDto);
        } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/list")
    public ResponseEntity<List<RequestBaseDto>> getRequests(@RequestParam(required = false, defaultValue = "-1") int requestPage,
                                                            @RequestParam(required = false, defaultValue = "-1") int requestAmount) {
        if ((requestAmount > maxRequestAmount) || (requestAmount <= 0)) {
            requestAmount = maxRequestAmount;
        }
        if ((requestPage <= 0)) {
            requestPage = 0;
        }
        List<RequestBaseDto> amountFrom = requestService.findAllRequestsBaseDto(requestPage, requestAmount);
        return ResponseEntity.ok(amountFrom);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RequestAllFieldsDto>> search(@RequestParam(value = "query") String query) {
        List<RequestAllFieldsDto> requestDtoList = requestService.search(query);
        return ResponseEntity.ok(requestDtoList);
    }
}
