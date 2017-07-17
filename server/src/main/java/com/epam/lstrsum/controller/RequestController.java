package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.security.EpamEmployeePrincipal;
import com.epam.lstrsum.service.RequestService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/request")
@ConfigurationProperties(prefix = "request")
public class RequestController {

    @Setter
    private int maxRequestAmount;

    @Autowired
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping()
    public ResponseEntity<String> addRequest(Authentication authentication, @RequestBody() RequestPostDto dtoObject)
            throws IOException {
        String email = authentication != null ? ((EpamEmployeePrincipal) (authentication.getPrincipal())).getEmail() : "John_Doe@epam.com";
        String requestId = requestService.addNewRequest(dtoObject, email);
        return ResponseEntity.ok(requestId);
    }

    @GetMapping(value = "/{requestId}")
    public ResponseEntity<RequestAllFieldsDto> getRequest(Authentication authentication, @PathVariable String requestId) {
        if (requestService.contains(requestId)) {
            RequestAllFieldsDto requestDto = requestService.getRequestDtoByRequestId(requestId);
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
}
